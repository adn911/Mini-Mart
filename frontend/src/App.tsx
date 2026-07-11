import { useEffect, useState } from "react";

import {
  getCart,
  addCartItem,
  updateCartItem,
  removeCartItem,
  getCategories,
  getProducts,
  getStoredToken,
  type CartItem,
  type CartResponse,
  type Category,
  type Product,
} from "./api";
import AdminConsole from "./components/AdminConsole";
import AdminLogin from "./components/AdminLogin";
import CartPanel from "./components/CartPanel";
import ProductCard from "./components/ProductCard";
import "./styles.css";

function useIsAdminRoute() {
  const [isAdmin, setIsAdmin] = useState(
    window.location.pathname.startsWith("/admin")
  );

  useEffect(() => {
    const handler = () => {
      setIsAdmin(window.location.pathname.startsWith("/admin"));
    };
    window.addEventListener("popstate", handler);
    return () => window.removeEventListener("popstate", handler);
  }, []);

  return isAdmin;
}

function navigate(path: string) {
  window.history.pushState({}, "", path);
  window.dispatchEvent(new PopStateEvent("popstate"));
}

function Storefront() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState("");
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | undefined>();
  const [loading, setLoading] = useState(true);
  const [cartOpen, setCartOpen] = useState(false);
  const [cart, setCart] = useState<CartResponse>({ items: [], itemCount: 0 });
  const [cartError, setCartError] = useState<string | undefined>();

  function refreshCart() {
    getCart()
      .then(setCart)
      .catch(() => setCart({ items: [], itemCount: 0 }));
  }

  useEffect(() => {
    refreshCart();
  }, []);

  useEffect(() => {
    getCategories()
      .then(setCategories)
      .catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    getProducts(search, selectedCategoryId)
      .then(setProducts)
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  }, [search, selectedCategoryId]);

  function handleAddToCart(productId: number) {
    setCartError(undefined);
    addCartItem(productId, 1)
      .then(() => {
        refreshCart();
        setCartOpen(true);
      })
      .catch((err: Error) => setCartError(err.message));
  }

  function handleUpdateQuantity(itemId: number, quantity: number) {
    if (quantity === 0) {
      handleRemoveItem(itemId);
      return;
    }
    setCartError(undefined);
    updateCartItem(itemId, quantity)
      .then(refreshCart)
      .catch((err: Error) => setCartError(err.message));
  }

  function handleRemoveItem(itemId: number) {
    setCartError(undefined);
    removeCartItem(itemId)
      .then(refreshCart)
      .catch((err: Error) => setCartError(err.message));
  }

  return (
    <div className="min-h-screen bg-white text-slate-900">
      <header className="border-b border-slate-200">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <h1 className="text-xl font-semibold tracking-tight">Mini-Mart</h1>
          <div className="flex items-center gap-4">
            <button
              onClick={() => { setCartError(undefined); setCartOpen(true); }}
              className="relative text-xs font-medium uppercase tracking-wider text-slate-500 transition-colors hover:text-slate-900"
            >
              Cart
              {cart.itemCount > 0 && (
                <span className="absolute -right-3 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-slate-900 text-[10px] text-white">
                  {cart.itemCount}
                </span>
              )}
            </button>
            <a
              href="/admin"
              onClick={(e) => { e.preventDefault(); navigate("/admin"); }}
              className="text-xs font-medium uppercase tracking-wider text-slate-500 transition-colors hover:text-slate-900"
            >
              Admin
            </a>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-8">
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="relative flex-1 max-w-md">
            <input
              type="text"
              placeholder="Search products..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full border border-slate-200 px-3 py-2 text-sm placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
            />
          </div>

          <div className="flex gap-2">
            <button
              onClick={() => setSelectedCategoryId(undefined)}
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
                onClick={() => setSelectedCategoryId(cat.id)}
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
        </div>

        {loading ? (
          <p className="text-sm text-slate-400">Loading products...</p>
        ) : products.length === 0 ? (
          <p className="text-sm text-slate-400">No products found.</p>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={handleAddToCart}
              />
            ))}
          </div>
        )}
      </main>

      <CartPanel
        items={cart.items}
        itemCount={cart.itemCount}
        open={cartOpen}
        onClose={() => setCartOpen(false)}
        onUpdateQuantity={handleUpdateQuantity}
        onRemoveItem={handleRemoveItem}
        error={cartError}
      />
    </div>
  );
}

export default function App() {
  const isAdmin = useIsAdminRoute();
  const [loggedIn, setLoggedIn] = useState(!!getStoredToken());

  if (!isAdmin) {
    return <Storefront />;
  }

  if (!loggedIn) {
    return <AdminLogin onLogin={() => setLoggedIn(true)} />;
  }

  return <AdminConsole onLogout={() => setLoggedIn(false)} />;
}
