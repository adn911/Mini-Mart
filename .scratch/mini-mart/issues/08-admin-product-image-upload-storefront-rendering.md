Status: ready-for-agent

# Admin product image upload and storefront rendering

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add authenticated product image upload and rendering. Admins should upload JPEG, PNG, or WebP images up to 5 MB. The backend should store files on the local filesystem, serve them through stable URLs, persist the retrievable image URL on the product, and render those images in both storefront and admin views.

This slice adds real product visual assets without implementing cleanup of unreferenced files.

## Acceptance criteria

- [ ] Authenticated admins can upload a product image.
- [ ] Image uploads require a valid admin JWT.
- [ ] JPEG, PNG, and WebP uploads are accepted.
- [ ] Unsupported image types are rejected with a clear bad-request response.
- [ ] Images larger than 5 MB are rejected with a clear bad-request response.
- [ ] Uploaded image files are stored under the backend uploads area for products.
- [ ] Uploaded images are served from stable product upload URLs.
- [ ] The product image URL/path is persisted on the product record.
- [ ] Replacing or deleting a product does not synchronously delete old image files.
- [ ] Storefront product cards render persisted product images.
- [ ] Admin product views render persisted product images and expose an upload control.
- [ ] Automated tests cover accepted uploads, rejected types, rejected oversized files, URL persistence, protected access, and image rendering behavior where practical.

## Blocked by

- .scratch/mini-mart/issues/03-admin-login-protected-console-shell.md
- .scratch/mini-mart/issues/04-admin-catalog-management-soft-delete-inventory.md
