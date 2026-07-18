# Category "More" Dropdown

## File to modify

`frontend/src/App.tsx`

## Changes

### 1. Add state (line ~61, after `cartExpired` state)

```ts
const [categoryDropdownOpen, setCategoryDropdownOpen] = useState(false);
```

### 2. Add visible limit constant (near top of Storefront component, after useState hooks)

```ts
const VISIBLE_CATEGORIES = 6;
```

### 3. Replace the category buttons block (lines 246-258)

**Before:**
```tsx
<div className="flex gap-2">
  <button
    onClick={() => { setSelectedCategoryId(undefined); setPage(0); }}
    className={`px-3 py-2 text-xs font-medium uppercase tracking-wider transition-colors ${
      selectedCategoryId === undefined
        ? "bg-slate-900 text-white"
        : "border border-slate-200 text-slate-600 hover:border-slate-400"
    }`}
  >
    All
  </button>
  {categories.map((cat) => (
    <button
      key={cat.id}
      onClick={() => { setSelectedCategoryId(cat.id); setPage(0); }}
      className={`px-3 py-2 text-xs font-medium uppercase tracking-wider transition-colors ${
        selectedCategoryId === cat.id
          ? "bg-slate-900 text-white"
          : "border border-slate-200 text-slate-600 hover:border-slate-400"
      }`}
    >
      {cat.name}
    </button>
  ))}
</div>
```

**After:**
```tsx
<div className="flex gap-2 flex-wrap items-center">
  <button
    onClick={() => { setSelectedCategoryId(undefined); setPage(0); }}
    className={`px-3 py-2 text-xs font-medium uppercase tracking-wider transition-colors ${
      selectedCategoryId === undefined
        ? "bg-slate-900 text-white"
        : "border border-slate-200 text-slate-600 hover:border-slate-400"
    }`}
  >
    All
  </button>
  {categories.slice(0, VISIBLE_CATEGORIES).map((cat) => (
    <button
      key={cat.id}
      onClick={() => { setSelectedCategoryId(cat.id); setPage(0); }}
      className={`px-3 py-2 text-xs font-medium uppercase tracking-wider transition-colors ${
        selectedCategoryId === cat.id
          ? "bg-slate-900 text-white"
          : "border border-slate-200 text-slate-600 hover:border-slate-400"
      }`}
    >
      {cat.name}
    </button>
  ))}
  {categories.length > VISIBLE_CATEGORIES && (
    <div className="relative">
      <button
        onClick={() => setCategoryDropdownOpen(!categoryDropdownOpen)}
        className="px-3 py-2 text-xs font-medium uppercase tracking-wider transition-colors border border-slate-200 text-slate-600 hover:border-slate-400"
      >
        More...
      </button>
      {categoryDropdownOpen && (
        <>
          <div className="fixed inset-0 z-10" onClick={() => setCategoryDropdownOpen(false)} />
          <div className="absolute right-0 top-full z-20 mt-1 w-48 border border-slate-200 bg-white shadow-lg">
            {categories.slice(VISIBLE_CATEGORIES).map((cat) => (
              <button
                key={cat.id}
                onClick={() => { setSelectedCategoryId(cat.id); setPage(0); setCategoryDropdownOpen(false); }}
                className={`block w-full px-3 py-2 text-left text-xs font-medium uppercase tracking-wider transition-colors ${
                  selectedCategoryId === cat.id ? "bg-slate-900 text-white" : "text-slate-600 hover:bg-slate-50"
                }`}
              >
                {cat.name}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  )}
</div>
```

## How it works

- First `VISIBLE_CATEGORIES` (6) categories stay as inline buttons — identical to current behavior.
- If there are more than 6, a "More..." button appears after them.
- Clicking "More..." opens a dropdown with the remaining categories.
- Clicking a dropdown item selects that category and closes the dropdown.
- Clicking the backdrop (fixed inset-0 div) closes the dropdown without selecting.
- Active category highlighting works the same in both the visible buttons and the dropdown.
