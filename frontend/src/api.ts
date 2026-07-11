export type Category = {
  id: number;
  name: string;
};

export type Product = {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  imageUrl: string;
  status: string;
  category: Category;
};

export type HealthResponse = {
  status: string;
  service: string;
};

export async function getBackendHealth(): Promise<HealthResponse> {
  const response = await fetch("/api/health");

  if (!response.ok) {
    throw new Error("Unable to reach Mini-Mart backend");
  }

  return response.json() as Promise<HealthResponse>;
}

export async function getCategories(): Promise<Category[]> {
  const response = await fetch("/api/categories");

  if (!response.ok) {
    throw new Error("Unable to fetch categories");
  }

  return response.json() as Promise<Category[]>;
}

export type LoginResponse = {
  token: string;
};

const TOKEN_KEY = "mini-mart-admin-token";

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function storeToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

async function authFetch(input: RequestInfo | URL, init?: RequestInit): Promise<Response> {
  const token = getStoredToken();
  const headers = { ...init?.headers } as Record<string, string>;
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return fetch(input, { ...init, headers });
}

export async function adminLogin(username: string, password: string): Promise<LoginResponse> {
  const response = await fetch("/api/admin/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Invalid credentials");
  }

  return response.json() as Promise<LoginResponse>;
}

export async function getAdminMe(): Promise<{ username: string }> {
  const response = await authFetch("/api/admin/me");

  if (!response.ok) {
    throw new Error("Unauthorized");
  }

  return response.json() as Promise<{ username: string }>;
}

export async function getAdminProducts(search?: string, categoryId?: number, includeDeleted?: boolean): Promise<Product[]> {
  const params = new URLSearchParams();
  if (search) params.set("search", search);
  if (categoryId) params.set("categoryId", String(categoryId));
  if (includeDeleted) params.set("includeDeleted", "true");

  const query = params.toString();
  const url = query ? `/api/admin/products?${query}` : "/api/admin/products";

  const response = await authFetch(url);
  if (!response.ok) throw new Error("Failed to fetch products");
  return response.json() as Promise<Product[]>;
}

export async function createProduct(data: Partial<Product>): Promise<Product> {
  const response = await authFetch("/api/admin/products", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!response.ok) throw new Error("Failed to create product");
  return response.json() as Promise<Product>;
}

export async function updateProduct(id: number, data: Partial<Product>): Promise<Product> {
  const response = await authFetch(`/api/admin/products/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!response.ok) throw new Error("Failed to update product");
  return response.json() as Promise<Product>;
}

export async function deleteProduct(id: number): Promise<void> {
  const response = await authFetch(`/api/admin/products/${id}`, { method: "DELETE" });
  if (!response.ok) throw new Error("Failed to delete product");
}

export async function refillProduct(id: number, quantity: number): Promise<Product> {
  const response = await authFetch(`/api/admin/products/${id}/refill`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ quantity }),
  });
  if (!response.ok) throw new Error("Failed to refill product");
  return response.json() as Promise<Product>;
}

export async function getAdminCategories(includeDeleted?: boolean): Promise<Category[]> {
  const url = includeDeleted ? "/api/admin/categories?includeDeleted=true" : "/api/admin/categories";
  const response = await authFetch(url);
  if (!response.ok) throw new Error("Failed to fetch categories");
  return response.json() as Promise<Category[]>;
}

export async function createCategory(name: string): Promise<Category> {
  const response = await authFetch("/api/admin/categories", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name }),
  });
  if (!response.ok) throw new Error("Failed to create category");
  return response.json() as Promise<Category>;
}

export async function updateCategory(id: number, name: string): Promise<Category> {
  const response = await authFetch(`/api/admin/categories/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name }),
  });
  if (!response.ok) throw new Error("Failed to update category");
  return response.json() as Promise<Category>;
}

export async function deleteCategory(id: number): Promise<void> {
  const response = await authFetch(`/api/admin/categories/${id}`, { method: "DELETE" });
  if (!response.ok) throw new Error("Failed to delete category");
}

export type PageResponse = {
  content: Product[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
};

export async function getProducts(search?: string, categoryId?: number, page = 0, size = 12): Promise<PageResponse> {
  const params = new URLSearchParams();
  if (search) params.set("search", search);
  if (categoryId) params.set("categoryId", String(categoryId));
  params.set("page", String(page));
  params.set("size", String(size));

  const url = `/api/products?${params.toString()}`;

  const response = await fetch(url);

  if (!response.ok) {
    throw new Error("Unable to fetch products");
  }

  return response.json() as Promise<PageResponse>;
}

export type CartItem = {
  id: number;
  product: Product;
  quantity: number;
};

export type CartResponse = {
  items: CartItem[];
  itemCount: number;
};

export async function getCart(): Promise<CartResponse> {
  const response = await fetch("/api/cart");
  if (!response.ok) throw new Error("Failed to fetch cart");
  return response.json() as Promise<CartResponse>;
}

export async function addCartItem(productId: number, quantity: number): Promise<CartItem> {
  const response = await fetch("/api/cart/items", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ productId, quantity }),
  });
  if (!response.ok) {
    const body = await response.json() as { error: string };
    throw new Error(body.error);
  }
  return response.json() as Promise<CartItem>;
}

export async function updateCartItem(itemId: number, quantity: number): Promise<CartItem | { status: string }> {
  const response = await fetch(`/api/cart/items/${itemId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ quantity }),
  });
  if (!response.ok) {
    const body = await response.json() as { error: string };
    throw new Error(body.error);
  }
  return response.json() as Promise<CartItem | { status: string }>;
}

export async function removeCartItem(itemId: number): Promise<void> {
  const response = await fetch(`/api/cart/items/${itemId}`, { method: "DELETE" });
  if (!response.ok) throw new Error("Failed to remove item");
}
