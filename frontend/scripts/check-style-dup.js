const fs = require('fs')
const path = require('path')

// Lightweight CSS rule splitter and normalizer.
// Not a full CSS parser but sufficient for the small scoped blocks in .vue files.
//
// The goal is to catch *meaningful* copy-pasted styling that should live in a
// shared stylesheet (styles/shared.css, styles/components.css, styles/forms.css)
// instead of being duplicated in several components. It deliberately ignores:
//   - blocks with fewer than MIN_DECLARATIONS properties (a one/two-line
//     collision like `flex-shrink: 0` is coincidental, not shared design)
//   - rules nested inside at-rules (@media / @supports / @keyframes): responsive
//     and prefers-reduced-motion overrides are expected to repeat across files.
//   - blocks made up exclusively of layout/spacing primitives (flexbox, grid,
//     gap, margins...): "a vertical stack with a 1rem gap" is not shared design
//     language, it is the same obvious solution to the same layout need.

const MIN_DECLARATIONS = 3

// Properties that only describe layout/spacing. A rule built entirely from these
// carries no visual identity worth centralising.
const LAYOUT_ONLY_PROPS = new Set([
  'display', 'flex', 'flex-direction', 'flex-wrap', 'flex-flow', 'flex-grow',
  'flex-shrink', 'flex-basis', 'gap', 'row-gap', 'column-gap',
  'align-items', 'align-content', 'align-self', 'justify-content', 'justify-items',
  'justify-self', 'place-items', 'place-content', 'order',
  'grid-template-columns', 'grid-template-rows', 'grid-template-areas',
  'grid-auto-flow', 'grid-auto-columns', 'grid-auto-rows',
  'margin', 'margin-top', 'margin-right', 'margin-bottom', 'margin-left'
])

const isLayoutOnly = (canon) =>
  canon.split(';').map(s => s.trim()).filter(Boolean).every(decl => {
    const prop = decl.split(':')[0].trim().toLowerCase()
    return LAYOUT_ONLY_PROPS.has(prop)
  })

// The button system (base look, hover/active/disabled, sizes) is centralised in
// styles/shared.css. Individual screens legitimately re-skin `.btn-primary` /
// `.btn-secondary` for their context, so a repeated tweak to those classes is
// expected, not a "move it to shared" finding.
const BUTTON_CLASSES = /(^|[\s>+~,])\.(btn-primary|btn-secondary|btn-danger|btn-icon)\b/
const isButtonReskin = (occurrences) =>
  occurrences.every(o => BUTTON_CLASSES.test(o.selector))

const stripComments = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '')

// Removes every `@rule ... { ... }` block (with balanced braces), so only the
// top-level rules of a scoped style block remain.
const stripAtRuleBlocks = (css) => {
  let out = ''
  for (let i = 0; i < css.length; i++) {
    if (css[i] === '@') {
      // find the block that opens after this at-rule and skip it whole
      let j = css.indexOf('{', i)
      if (j === -1) { break }
      let depth = 1
      j++
      while (j < css.length && depth > 0) {
        if (css[j] === '{') depth++
        else if (css[j] === '}') depth--
        j++
      }
      i = j - 1
      continue
    }
    out += css[i]
  }
  return out
}

const normalizeDecls = (decls) => {
  return decls
    .split(/;+/)
    .map(s => s.trim())
    .filter(Boolean)
    .map(s => s.replace(/\s*:\s*/, ' : '))
    .sort()
    .join('; ')
}

const countDecls = (canon) => (canon ? canon.split(';').filter(Boolean).length : 0)

const extractScopedStyles = (content) => {
  const re = /<style[^>]*scoped[^>]*>([\s\S]*?)<\/style>/g
  let m, out = []
  while ((m = re.exec(content)) !== null) out.push(m[1])
  return out
}

const scanDir = (dir) => {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  let files = []
  for (const e of entries) {
    const full = path.join(dir, e.name)
    if (e.isDirectory()) files = files.concat(scanDir(full))
    else if (e.isFile() && e.name.endsWith('.vue')) files.push(full)
  }
  return files
}

const files = scanDir(path.join(__dirname, '..', 'src'))

// Map canonical declaration set -> [ { file, selector } ]
const declMap = new Map()

for (const f of files) {
  const txt = fs.readFileSync(f, 'utf8')
  const blocks = extractScopedStyles(txt)
  for (const rawBlock of blocks) {
    const block = stripAtRuleBlocks(stripComments(rawBlock))
    const ruleRe = /([^{}]+)\{([^{}]+)\}/g
    let m
    while ((m = ruleRe.exec(block)) !== null) {
      const selector = m[1].trim().replace(/\s+/g, ' ')
      const decls = m[2].trim()
      const canon = normalizeDecls(decls)
      if (!canon || countDecls(canon) < MIN_DECLARATIONS) continue
      if (isLayoutOnly(canon)) continue
      const arr = declMap.get(canon) || []
      arr.push({ file: path.relative(process.cwd(), f), selector })
      declMap.set(canon, arr)
    }
  }
}

const duplicates = []
for (const [canon, arr] of declMap.entries()) {
  if (arr.length > 1) {
    const filesSet = new Set(arr.map(a => a.file))
    if (filesSet.size > 1 && !isButtonReskin(arr)) duplicates.push({ canon, occurrences: arr })
  }
}

if (duplicates.length) {
  console.error('Detected duplicated CSS declaration blocks across components:')
  for (const d of duplicates) {
    console.error('\n-----')
    console.error('Declarations:', d.canon)
    for (const o of d.occurrences) console.error('-', o.file, ' selector:', o.selector)
  }
  console.error('\nSuggestion: move the shared declarations to frontend/src/styles/shared.css or components.css and use helper classes instead of duplicating scoped rules.')
  process.exit(2)
} else {
  console.log('Style check passed')
}
