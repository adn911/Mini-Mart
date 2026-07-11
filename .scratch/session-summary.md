## Goal
- Find free-to-use Unsplash photo URLs for 24 grocery products for demo app seed data, formatted as `https://images.unsplash.com/photo-{ID}?w=400&h=400&fit=crop`

## Constraints & Preferences
- URLs must be from images.unsplash.com domain
- No code should be written
- Close alternatives acceptable if exact match unavailable
- Results should be simple numbered list with product name and full URL

## Progress
### Done
- All 24 products have verified, product-matched photo IDs (HTTP 200 with `?w=400&h=400&fit=crop`):

  1.  **Sparkling Water:** `https://images.unsplash.com/photo-1619622683714-5cb5e905d6aa?w=400&h=400&fit=crop`
  2.  **Cold Brew Coffee:** `https://images.unsplash.com/photo-1765690835487-8da60d318d0f?w=400&h=400&fit=crop`
  3.  **Green Tea:** `https://images.unsplash.com/photo-1501199532894-9449c0a85a77?w=400&h=400&fit=crop`
  4.  **Whole Milk:** `https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&h=400&fit=crop`
  5.  **Bananas:** `https://images.unsplash.com/photo-1513563401345-1123a773541c?w=400&h=400&fit=crop`
  6.  **Almonds:** `https://images.unsplash.com/photo-1514573359475-f0b1c80638e6?w=400&h=400&fit=crop`
  7.  **Dark Chocolate Bar:** `https://images.unsplash.com/photo-1623660053975-cf75a8be0908?w=400&h=400&fit=crop`
  8.  **Olive Oil:** `https://images.unsplash.com/photo-1599451897608-ad6eb8676edf?w=400&h=400&fit=crop`
  9.  **Basmati Rice:** `https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&h=400&fit=crop`
  10. **Pasta:** `https://images.unsplash.com/photo-1473093226795-af9932fe5856?w=400&h=400&fit=crop`
  11. **Greek Yogurt:** `https://images.unsplash.com/photo-1571212515416-fef01fc43637?w=400&h=400&fit=crop`
  12. **Frozen Pizza:** `https://images.unsplash.com/photo-1761207850834-69151e9bc810?w=400&h=400&fit=crop`
  13. **Avocados:** `https://images.unsplash.com/photo-1531880567437-5bfef553f822?w=400&h=400&fit=crop`
  14. **Atlantic Salmon:** `https://images.unsplash.com/photo-1754220990618-ce43c31ad266?w=400&h=400&fit=crop`
  15. **Ground Beef:** `https://images.unsplash.com/photo-1448907503123-67254d59ca4f?w=400&h=400&fit=crop`
  16. **Trail Mix:** `https://images.unsplash.com/photo-1607664608695-45aaa6d621fc?w=400&h=400&fit=crop`
  17. **Free-Range Eggs:** `https://images.unsplash.com/photo-1664292006878-c3e2d780ffc2?w=400&h=400&fit=crop`
  18. **Sourdough Loaf:** `https://images.unsplash.com/photo-1597604391235-a7429b4b350c?w=400&h=400&fit=crop`
  19. **Croissants:** `https://images.unsplash.com/photo-1623334044303-241021148842?w=400&h=400&fit=crop`
  20. **Blueberry Muffins:** `https://images.unsplash.com/photo-1637087788449-222cf1da072a?w=400&h=400&fit=crop`
  21. **Vanilla Ice Cream:** `https://images.unsplash.com/photo-1560008581-09826d1de69e?w=400&h=400&fit=crop`
  22. **Frozen Peas:** `https://images.unsplash.com/photo-1592394533824-9440e5d68530?w=400&h=400&fit=crop`
  23. **Cherry Tomatoes:** `https://images.unsplash.com/photo-1524593166156-312f362cada0?w=400&h=400&fit=crop`
  24. **Chicken Breast:** `https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400&h=400&fit=crop`

- Map pattern: `unsplash.com/photos/{short_id}/download?force=true` → `images.unsplash.com/photo-{long_id}?...`
- 34 additional verified photo IDs (HTTP 200) available as alternative candidates

### Blocked
- Unsplash+ (premium) photos block the download redirect (timeout); only free/public photos work
- Searching `unsplash.com/photos/` pages for short IDs via web search returns massive HTML, hard to parse

## Key Decisions
- Use download redirect method (`{short_id}/download?force=true`) to map short IDs to long photo IDs
- Avoid Unsplash+ photos; only free photos work with redirect
- Prefer web search for product-specific short IDs over direct Unsplash page fetches

## Relevant Files
- (none; this is pure data collection, no code changes)
