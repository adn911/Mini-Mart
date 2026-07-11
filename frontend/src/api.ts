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

export async function getProducts(search?: string, categoryId?: number): Promise<Product[]> {
  const params = new URLSearchParams();
  if (search) params.set("search", search);
  if (categoryId) params.set("categoryId", String(categoryId));

  const query = params.toString();
  const url = query ? `/api/products?${query}` : "/api/products";

  const response = await fetch(url);

  if (!response.ok) {
    throw new Error("Unable to fetch products");
  }

  return response.json() as Promise<Product[]>;
}
