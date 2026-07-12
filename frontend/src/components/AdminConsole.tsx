import { useEffect, useState } from "react";

import {
  clearToken,
  createCategory,
  createProduct,
  deleteCategory as deleteCategoryApi,
  deleteProduct as deleteProductApi,
  getAdminCategories,
  getAdminOrders,
  getAdminProducts,
  refillProduct,
  updateCategory as updateCategoryApi,
  updateOrderStatus,
  updateProduct as updateProductApi,
  uploadProductImage,
  type Category,
  type OrderResponse,
  type Product,
} from "../api";

type AdminConsoleProps = {
  onLogout: () => void;
};

const LOW_STOCK_THRESHOLD = 10;

function ProductForm({
  product,
  categories,
  onSave,
  onCancel,
  onImageUploaded,
}: {
  product?: Product;
  categories: Category[];
  onSave: (data: Partial<Product>) => Promise<void>;
  onCancel: () => void;
  onImageUploaded?: () => void;
}) {
  const [name, setName] = useState(product?.name ?? "");
  const [description, setDescription] = useState(product?.description ?? "");
  const [price, setPrice] = useState(String(product?.price ?? ""));
  const [stockQuantity, setStockQuantity] = useState(String(product?.stockQuantity ?? ""));
  const [categoryId, setCategoryId] = useState(String(product?.category?.id ?? ""));
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [imageUrl, setImageUrl] = useState(product?.imageUrl ?? "");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    await onSave({
      name,
      description,
      price: parseFloat(price),
      stockQuantity: parseInt(stockQuantity) || 0,
      category: categoryId ? { id: parseInt(categoryId) } as Category : undefined,
    });
    setSaving(false);
  };

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !product?.id) return;

    setUploading(true);
    setUploadError(null);
    try {
      const result = await uploadProductImage(product.id, file);
      setImageUrl(result.imageUrl);
      onImageUploaded?.();
    } catch (err) {
      setUploadError(err instanceof Error ? err.message : "Upload failed");
    }
    setUploading(false);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-slate-500">Name</label>
        <input value={name} onChange={(e) => setName(e.target.value)} required
          className="w-full border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none" />
      </div>
      <div>
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-slate-500">Description</label>
        <textarea value={description} onChange={(e) => setDescription(e.target.value)}
          className="w-full border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none" rows={3} />
      </div>
      <div className="flex gap-4">
        <div className="flex-1">
          <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-slate-500">Price</label>
          <input type="number" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} required
            className="w-full border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none" />
        </div>
        <div className="flex-1">
          <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-slate-500">Stock</label>
          <input type="number" value={stockQuantity} onChange={(e) => setStockQuantity(e.target.value)} required
            className="w-full border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none" />
        </div>
      </div>
      <div>
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-slate-500">Category</label>
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}
          className="w-full border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none">
          <option value="">None</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </div>
      {product && (
        <div>
          <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-slate-500">Image</label>
          <div className="flex items-start gap-4">
            {imageUrl ? (
              <img src={imageUrl} alt={product.name}
                className="h-20 w-20 rounded border border-slate-200 object-cover" />
            ) : (
              <div className="flex h-20 w-20 items-center justify-center rounded border border-slate-200 bg-slate-50 text-xs text-slate-400">
                No image
              </div>
            )}
            <div className="flex flex-col gap-1">
              <label className="cursor-pointer border border-slate-200 px-3 py-1.5 text-xs font-medium uppercase tracking-wider text-slate-600 transition-colors hover:border-slate-400">
                {uploading ? "Uploading..." : "Choose file"}
                <input type="file" accept="image/jpeg,image/png,image/webp"
                  onChange={handleUpload} className="hidden" disabled={uploading} />
              </label>
              {uploadError && <p className="text-xs text-red-500">{uploadError}</p>}
            </div>
          </div>
        </div>
      )}
      <div className="flex gap-2 pt-2">
        <button type="submit" disabled={saving}
          className="bg-slate-900 px-4 py-2 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90 disabled:opacity-50">
          {saving ? "Saving..." : "Save"}
        </button>
        <button type="button" onClick={onCancel}
          className="border border-slate-200 px-4 py-2 text-xs font-medium uppercase tracking-wider text-slate-600 hover:border-slate-400">
          Cancel
        </button>
      </div>
    </form>
  );
}

function CategoryForm({
  category,
  onSave,
  onCancel,
}: {
  category?: Category;
  onSave: (name: string) => Promise<void>;
  onCancel: () => void;
}) {
  const [name, setName] = useState(category?.name ?? "");
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    await onSave(name);
    setSaving(false);
  };

  return (
    <form onSubmit={handleSubmit} className="flex items-end gap-3">
      <div className="flex-1">
        <label className="mb-1 block text-xs font-medium uppercase tracking-wider text-slate-500">Name</label>
        <input value={name} onChange={(e) => setName(e.target.value)} required
          className="w-full border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none" />
      </div>
      <button type="submit" disabled={saving}
        className="bg-slate-900 px-4 py-2 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90 disabled:opacity-50">
        {saving ? "Saving..." : "Save"}
      </button>
      <button type="button" onClick={onCancel}
        className="border border-slate-200 px-4 py-2 text-xs font-medium uppercase tracking-wider text-slate-600 hover:border-slate-400">
        Cancel
      </button>
    </form>
  );
}

export default function AdminConsole({ onLogout }: AdminConsoleProps) {
  const [tab, setTab] = useState<"products" | "categories" | "orders">("products");
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState("");
  const [includeDeleted, setIncludeDeleted] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | undefined>();
  const [showProductForm, setShowProductForm] = useState(false);
  const [refillId, setRefillId] = useState<number | null>(null);
  const [refillQty, setRefillQty] = useState("10");
  const [editingCategory, setEditingCategory] = useState<Category | undefined>();
  const [showCategoryForm, setShowCategoryForm] = useState(false);
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [orderSearch, setOrderSearch] = useState("");
  const [orderStatusFilter, setOrderStatusFilter] = useState("");
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [confirmingOrderId, setConfirmingOrderId] = useState<number | null>(null);
  const [orderActionError, setOrderActionError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadProducts = async () => {
    setLoading(true);
    try {
      const data = await getAdminProducts(search || undefined, undefined, includeDeleted);
      setProducts(data);
    } catch {
      setProducts([]);
    }
    setLoading(false);
  };

  const loadCategories = async () => {
    try {
      const data = await getAdminCategories(true);
      setCategories(data);
    } catch {
      setCategories([]);
    }
  };

  const loadOrders = async () => {
    setOrdersLoading(true);
    try {
      const data = await getAdminOrders(orderSearch || undefined, orderStatusFilter || undefined);
      setOrders(data);
    } catch {
      setOrders([]);
    }
    setOrdersLoading(false);
  };

  const handleConfirmOrder = async (id: number) => {
    setOrderActionError(null);
    setConfirmingOrderId(id);
    try {
      await updateOrderStatus(id, "CONFIRMED");
      await loadOrders();
    } catch {
      setOrderActionError("Failed to confirm order");
    }
    setConfirmingOrderId(null);
  };

  useEffect(() => { setError(null); loadProducts(); }, [search, includeDeleted]);
  useEffect(() => { loadCategories(); }, []);
  useEffect(() => { loadOrders(); }, [orderSearch, orderStatusFilter]);

  const handleSaveProduct = async (data: Partial<Product>) => {
    try {
      setError(null);
      if (editingProduct) {
        await updateProductApi(editingProduct.id, data);
      } else {
        await createProduct(data);
      }
      setShowProductForm(false);
      setEditingProduct(undefined);
      await loadProducts();
    } catch (e) {
      setError("Failed to save product: " + (e instanceof Error ? e.message : String(e)));
    }
  };

  const handleDeleteProduct = async (id: number) => {
    try {
      setError(null);
      await deleteProductApi(id);
      await loadProducts();
    } catch {
      setError("Failed to delete product");
    }
  };

  const handleRefill = async (id: number) => {
    try {
      setError(null);
      await refillProduct(id, parseInt(refillQty) || 10);
      setRefillId(null);
      setRefillQty("10");
      await loadProducts();
    } catch {
      setError("Failed to refill product");
    }
  };

  const handleSaveCategory = async (name: string) => {
    try {
      setError(null);
      if (editingCategory) {
        await updateCategoryApi(editingCategory.id, name);
      } else {
        await createCategory(name);
      }
      setShowCategoryForm(false);
      setEditingCategory(undefined);
      await loadCategories();
    } catch {
      setError("Failed to save category");
    }
  };

  const handleDeleteCategory = async (id: number) => {
    try {
      setError(null);
      await deleteCategoryApi(id);
      await loadCategories();
    } catch {
      setError("Failed to delete category");
    }
  };

  const handleLogout = () => {
    clearToken();
    onLogout();
  };

  return (
    <div className="min-h-screen bg-white text-slate-900">
      <header className="border-b border-slate-200">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <h1 className="text-xl font-semibold tracking-tight">Admin Console</h1>
          <button onClick={handleLogout}
            className="border border-slate-200 px-3 py-1.5 text-xs font-medium uppercase tracking-wider text-slate-600 transition-colors hover:border-slate-400">
            Sign out
          </button>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6 py-4">
        {error && (
          <div className="mb-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-600">
            {error}
          </div>
        )}
        <div className="mb-6 flex gap-4 border-b border-slate-200">
          <button onClick={() => setTab("products")}
            className={`pb-3 text-xs font-medium uppercase tracking-wider transition-colors ${tab === "products" ? "border-b-2 border-slate-900 text-slate-900" : "text-slate-500 hover:text-slate-900"}`}>
            Products
          </button>
          <button onClick={() => setTab("categories")}
            className={`pb-3 text-xs font-medium uppercase tracking-wider transition-colors ${tab === "categories" ? "border-b-2 border-slate-900 text-slate-900" : "text-slate-500 hover:text-slate-900"}`}>
            Categories
          </button>
          <button onClick={() => setTab("orders")}
            className={`pb-3 text-xs font-medium uppercase tracking-wider transition-colors ${tab === "orders" ? "border-b-2 border-slate-900 text-slate-900" : "text-slate-500 hover:text-slate-900"}`}>
            Orders
          </button>
        </div>

        {tab === "products" && (
          <div>
            {!showProductForm && (
              <>
                <div className="mb-4 flex flex-wrap items-center gap-3">
                  <input type="text" placeholder="Search products..." value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="flex-1 min-w-[200px] border border-slate-200 px-3 py-2 text-sm placeholder:text-slate-400 focus:border-slate-400 focus:outline-none" />
                  <label className="flex items-center gap-2 text-xs text-slate-600">
                    <input type="checkbox" checked={includeDeleted}
                      onChange={(e) => setIncludeDeleted(e.target.checked)} />
                    Show deleted
                  </label>
                  <button onClick={() => { setEditingProduct(undefined); setShowProductForm(true); }}
                    className="bg-slate-900 px-4 py-2 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90">
                    + New Product
                  </button>
                </div>

                {loading ? (
                  <p className="text-sm text-slate-400">Loading...</p>
                ) : products.length === 0 ? (
                  <p className="text-sm text-slate-400">No products found.</p>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-slate-200 text-left text-xs font-medium uppercase tracking-wider text-slate-500">
                          <th className="pb-3 pr-4">Image</th>
                          <th className="pb-3 pr-4">Name</th>
                          <th className="pb-3 pr-4">Category</th>
                          <th className="pb-3 pr-4">Price</th>
                          <th className="pb-3 pr-4">Stock</th>
                          <th className="pb-3 pr-4">Reserved</th>
                          <th className="pb-3 pr-4">Available</th>
                          <th className="pb-3 pr-4">Status</th>
                          <th className="pb-3">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {products.map((p) => (
                          <tr key={p.id} className="border-b border-slate-100">
                            <td className="py-3 pr-4">
                              {p.imageUrl ? (
                                <img src={p.imageUrl} alt={p.name}
                                  className="h-10 w-10 rounded object-cover" />
                              ) : (
                                <div className="h-10 w-10 rounded bg-slate-50" />
                              )}
                            </td>
                            <td className="py-3 pr-4 font-medium">{p.name}</td>
                            <td className="py-3 pr-4 text-slate-500">{p.category?.name ?? "—"}</td>
                            <td className="py-3 pr-4">${p.price.toFixed(2)}</td>
                            <td className={`py-3 pr-4 ${p.availableQuantity < LOW_STOCK_THRESHOLD ? "text-amber-600" : ""}`}>
                              {p.stockQuantity}
                              {p.availableQuantity < LOW_STOCK_THRESHOLD && p.status === "ACTIVE" && (
                                <span className="ml-2 text-[10px] uppercase tracking-wider text-amber-500">Low</span>
                              )}
                            </td>
                            <td className="py-3 pr-4 text-slate-500">{p.reservedQuantity}</td>
                            <td className={`py-3 pr-4 ${p.availableQuantity < LOW_STOCK_THRESHOLD ? "text-amber-600" : ""}`}>
                              {p.availableQuantity}
                            </td>
                            <td className="py-3 pr-4">
                              <span className={`text-xs uppercase tracking-wider ${p.status === "DELETED" ? "text-red-400" : "text-slate-500"}`}>
                                {p.status}
                              </span>
                            </td>
                            <td className="py-3">
                              <div className="flex gap-2">
                                <button onClick={() => { setEditingProduct(p); setShowProductForm(true); }}
                                  className="text-xs text-slate-500 underline hover:text-slate-900">
                                  Edit
                                </button>
                                {p.status === "ACTIVE" && (
                                  <button onClick={() => handleDeleteProduct(p.id)}
                                    className="text-xs text-red-400 underline hover:text-red-600">
                                    Delete
                                  </button>
                                )}
                                {p.status === "ACTIVE" && (
                                  <>
                                    {refillId === p.id ? (
                                      <span className="flex gap-1">
                                        <input type="number" value={refillQty}
                                          onChange={(e) => setRefillQty(e.target.value)}
                                          className="w-16 border border-slate-200 px-2 py-0.5 text-xs" />
                                        <button onClick={() => handleRefill(p.id)}
                                          className="text-xs text-slate-900 underline">Go</button>
                                        <button onClick={() => setRefillId(null)}
                                          className="text-xs text-slate-400 underline">X</button>
                                      </span>
                                    ) : (
                                      <button onClick={() => { setRefillId(p.id); setRefillQty("10"); }}
                                        className="text-xs text-slate-500 underline hover:text-slate-900">
                                        Refill
                                      </button>
                                    )}
                                  </>
                                )}
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </>
            )}

            {showProductForm && (
              <div>
                <h2 className="mb-4 text-base font-medium">{editingProduct ? "Edit Product" : "New Product"}</h2>
                <ProductForm
                  product={editingProduct}
                  categories={categories}
                  onSave={handleSaveProduct}
                  onCancel={() => { setShowProductForm(false); setEditingProduct(undefined); }}
                  onImageUploaded={loadProducts}
                />
              </div>
            )}
          </div>
        )}

        {tab === "categories" && (
          <div>
            {!showCategoryForm && (
              <>
                <div className="mb-4 flex items-center gap-3">
                  <button onClick={() => { setEditingCategory(undefined); setShowCategoryForm(true); }}
                    className="bg-slate-900 px-4 py-2 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90">
                    + New Category
                  </button>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-slate-200 text-left text-xs font-medium uppercase tracking-wider text-slate-500">
                        <th className="pb-3 pr-4">Name</th>
                        <th className="pb-3 pr-4">Status</th>
                        <th className="pb-3">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {categories.map((c) => (
                        <tr key={c.id} className="border-b border-slate-100">
                          <td className="py-3 pr-4 font-medium">{c.name}</td>
                          <td className="py-3 pr-4">
                            <span className={`text-xs uppercase tracking-wider ${c.status === "DELETED" ? "text-red-400" : "text-slate-500"}`}>
                              {c.status}
                            </span>
                          </td>
                          <td className="py-3">
                            <div className="flex gap-2">
                              <button onClick={() => { setEditingCategory(c); setShowCategoryForm(true); }}
                                className="text-xs text-slate-500 underline hover:text-slate-900">
                                Edit
                              </button>
                              {c.status === "ACTIVE" && (
                                <button onClick={() => handleDeleteCategory(c.id)}
                                  className="text-xs text-red-400 underline hover:text-red-600">
                                  Delete
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            )}

            {showCategoryForm && (
              <div>
                <h2 className="mb-4 text-base font-medium">{editingCategory ? "Edit Category" : "New Category"}</h2>
                <CategoryForm
                  category={editingCategory}
                  onSave={handleSaveCategory}
                  onCancel={() => { setShowCategoryForm(false); setEditingCategory(undefined); }}
                />
              </div>
            )}
          </div>
        )}

        {tab === "orders" && (
          <div>
            <div className="mb-4 flex flex-wrap items-center gap-3">
              <input type="text" placeholder="Search by session ID..." value={orderSearch}
                onChange={(e) => setOrderSearch(e.target.value)}
                className="flex-1 min-w-[200px] border border-slate-200 px-3 py-2 text-sm placeholder:text-slate-400 focus:border-slate-400 focus:outline-none" />
              <select value={orderStatusFilter} onChange={(e) => setOrderStatusFilter(e.target.value)}
                className="border border-slate-200 px-3 py-2 text-sm focus:border-slate-400 focus:outline-none">
                <option value="">All statuses</option>
                <option value="PLACED">PLACED</option>
                <option value="CONFIRMED">CONFIRMED</option>
                <option value="SHIPPED">SHIPPED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>

            {orderActionError && (
              <div className="mb-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-600">
                {orderActionError}
              </div>
            )}

            {ordersLoading ? (
              <p className="text-sm text-slate-400">Loading...</p>
            ) : orders.length === 0 ? (
              <p className="text-sm text-slate-400">No orders found.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-slate-200 text-left text-xs font-medium uppercase tracking-wider text-slate-500">
                      <th className="pb-3 pr-4">Order #</th>
                      <th className="pb-3 pr-4">Name</th>
                      <th className="pb-3 pr-4">City</th>
                      <th className="pb-3 pr-4">Session</th>
                      <th className="pb-3 pr-4">Status</th>
                      <th className="pb-3 pr-4">Payment</th>
                      <th className="pb-3 pr-4">Total</th>
                      <th className="pb-3 pr-4">Items</th>
                      <th className="pb-3 pr-4">Date</th>
                      <th className="pb-3">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.map((o) => (
                      <tr key={o.id} className="border-b border-slate-100">
                        <td className="py-3 pr-4 font-medium">#{o.id}</td>
                        <td className="py-3 pr-4 text-slate-500">{(o.firstName ?? "") + " " + (o.lastName ?? "")}</td>
                        <td className="py-3 pr-4 text-slate-500">{o.city ?? ""}</td>
                        <td className="py-3 pr-4 text-slate-500 font-mono text-[11px]">{(o.sessionId ?? "").substring(0, 16)}...</td>
                        <td className="py-3 pr-4">
                          <span className="text-xs uppercase tracking-wider text-slate-500">{o.status}</span>
                        </td>
                        <td className="py-3 pr-4 text-slate-500 text-xs">{(o.paymentMethod ?? "CASH_ON_DELIVERY").replace(/_/g, " ")}</td>
                        <td className="py-3 pr-4">${o.total.toFixed(2)}</td>
                        <td className="py-3 pr-4 text-slate-500">{o.items.length}</td>
                        <td className="py-3 text-slate-500 text-xs">{new Date(o.createdAt).toLocaleString()}</td>
                        <td className="py-3 pl-4">
                          {o.status === "PLACED" && (
                            <button
                              onClick={() => handleConfirmOrder(o.id)}
                              disabled={confirmingOrderId === o.id}
                              className="text-xs font-medium uppercase tracking-wider text-emerald-600 transition-colors hover:text-emerald-700 disabled:opacity-50"
                            >
                              {confirmingOrderId === o.id ? "..." : "Confirm"}
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
