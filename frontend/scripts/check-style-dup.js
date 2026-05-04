const fs = require('fs')
const path = require('path')

// Lightweight CSS rule splitter and normalizer.
// Not a full CSS parser but sufficient for small scoped blocks.
const normalizeDecls = (decls) => {
  return decls
    .split(/;+/)
    .map(s => s.trim())
    .filter(Boolean)
    .map(s => s.replace(/\s*:\s*/,' : '))
    .sort()
    .join('; ')
}

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
  for (const block of blocks) {
    // very small CSS rule splitter: selector { decls }
    const ruleRe = /([^{]+)\{([^}]+)\}/g
    let m
    while ((m = ruleRe.exec(block)) !== null) {
      const selector = m[1].trim()
      const decls = m[2].trim()
      const canon = normalizeDecls(decls)
      if (!canon) continue
      const key = canon
      const arr = declMap.get(key) || []
      arr.push({ file: path.relative(process.cwd(), f), selector })
      declMap.set(key, arr)
    }
  }
}

const duplicates = []
for (const [canon, arr] of declMap.entries()) {
  if (arr.length > 1) {
    // Only consider duplicates coming from different files or different selectors
    const filesSet = new Set(arr.map(a => a.file))
    if (filesSet.size > 1) duplicates.push({ canon, occurrences: arr })
  }
}

if (duplicates.length) {
  console.error('Detected duplicated CSS declaration blocks across components:')
  for (const d of duplicates) {
    console.error('\n-----')
    console.error('Declarations:', d.canon)
    for (const o of d.occurrences) console.error('-', o.file, ' selector:', o.selector)
  }
  console.error('\nSuggestion: move the shared declarations to frontend/src/styles/forms.css or shared.css and use helper classes instead of duplicating scoped rules.')
  process.exit(2)
} else {
  console.log('Style check passed')
}
