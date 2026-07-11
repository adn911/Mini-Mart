import { useEffect, useState } from "react";

import {
  clearToken,
  createCategory,
  createProduct,
  deleteCategory as deleteCategoryApi,
  deleteProduct as deleteProductApi,
  getAdminCategories,
  getAdminProducts,
  refillProduct,
  updateCategory as updateCategoryApi,
  updateProduct as updateProductApi,
  type Category,
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
}: {
  product?: Product;
  categories: Category[];
  onSave: (data: Partial<Product>) => Promise<void>;
  onCancel: () => void;
}) {
  const [name, setName] = useState(product?.name ?? "");
  const [description, setDescription] = useState(product?.description ?? "");
  const [price, setPrice] = useState(String(product?.price ?? ""));
  const [stockQuantity, setStockQuantity] = useState(String(product?.stockQuantity ?? ""));
  const [categoryId, setCategoryId] = useState(String(product?.category?.id ?? ""));
  const [saving, setSaving] = useState(false);

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
  const [tab, setTab] = useState<"products" | "categories">("products");
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
  const [loading, setLoading] = useState(true);

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

  useEffect(() => { loadProducts(); }, [search, includeDeleted]);
  useEffect(() => { loadCategories(); }, []);

  const handleSaveProduct = async (data: Partial<Product>) => {
    try {
      if (editingProduct) {
        await updateProductApi(editingProduct.id, data);
      } else {
        await createProduct(data);
      }
      setShowProductForm(false);
      setEditingProduct(undefined);
      await loadProducts();
    } catch (e) {
      alert("Failed to save product: " + (e instanceof Error ? e.message : String(e)));
    }
  };

  const handleDeleteProduct = async (id: number) => {
    try {
      await deleteProductApi(id);
      await loadProducts();
    } catch {
      alert("Failed to delete product");
    }
  };

  const handleRefill = async (id: number) => {
    try {
      await refillProduct(id, parseInt(refillQty) || 10);
      setRefillId(null);
      setRefillQty("10");
      await loadProducts();
    } catch {
      alert("Failed to refill product");
    }
  };

  const handleSaveCategory = async (name: string) => {
    try {
      if (editingCategory) {
        await updateCategoryApi(editingCategory.id, name);
      } else {
        await createCategory(name);
      }
      setShowCategoryForm(false);
      setEditingCategory(undefined);
      await loadCategories();
    } catch {
      alert("Failed to save category");
    }
  };

  const handleDeleteCategory = async (id: number) => {
    try {
      await deleteCategoryApi(id);
      await loadCategories();
    } catch {
      alert("Failed to delete category");
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
        <div className="mb-6 flex gap-4 border-b border-slate-200">
          <button onClick={() => setTab("products")}
            className={`pb-3 text-xs font-medium uppercase tracking-wider transition-colors ${tab === "products" ? "border-b-2 border-slate-900 text-slate-900" : "text-slate-500 hover:text-slate-900"}`}>
            Products
          </button>
          <button onClick={() => setTab("categories")}
            className={`pb-3 text-xs font-medium uppercase tracking-wider transition-colors ${tab === "categories" ? "border-b-2 border-slate-900 text-slate-900" : "text-slate-500 hover:text-slate-900"}`}>
            Categories
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
      </div>
    </div>
  );
}
