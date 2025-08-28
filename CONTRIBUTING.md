# Frontend CSS helpers

This project includes a small set of shared CSS helper classes to keep UI consistent and avoid style duplication.

Files
- `frontend/src/styles/variables.css` — design tokens (colors, spacing, radii).
- `frontend/src/styles/shared.css` — base layout and shared helpers used app-wide.
- `frontend/src/styles/forms.css` — small documented utilities for forms (form-row, form-label, form-actions, hint).

Guidelines
- Prefer the helper classes instead of copying small layout styles into components.
- Use `aria-describedby` to link hint text to inputs, and use `.visually-hidden` for screen-reader-only labels.
- If you add a new utility, document it in `forms.css` and include a short example here.

Example

```html
<label class="form-label" for="foo">Campo</label>
<textarea id="foo" aria-describedby="foo-hint"></textarea>
<p id="foo-hint" class="hint">Ayuda para el campo</p>
<div class="form-actions form-actions--right">
  <button>Guardar</button>
</div>
```

Rules
- Keep helper classes small and composable. Avoid putting complex component layout rules here.
- If rules are specific to a single component, keep them scoped in the component's `<style scoped>`.

Thank you for keeping the UI consistent!
