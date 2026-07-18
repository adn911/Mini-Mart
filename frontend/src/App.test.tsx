import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, test, vi } from "vitest";

import App from "./App";

const beverages = { id: 1, name: "Beverages", status: "ACTIVE" };
const snacks = { id: 2, name: "Snacks", status: "ACTIVE" };
const pantry = { id: 3, name: "Pantry", status: "ACTIVE" };

const mockCategories = [beverages, snacks, pantry];

const baseProduct = {
  discountPercent: undefined,
  effectivePrice: 0,
  onSale: false,
};

const sparklingWater = {
  ...baseProduct,
  id: 1,
  name: "Sparkling Water",
  description: "Premium sparkling mineral water",
  price: 4.99,
  effectivePrice: 4.99,
  stockQuantity: 100,
  reservedQuantity: 0,
  availableQuantity: 100,
  imageUrl: "https://picsum.photos/seed/sparkling-water/400/400",
  status: "ACTIVE",
  category: beverages,
};

const darkChocolate = {
  ...baseProduct,
  id: 2,
  name: "Dark Chocolate Bar",
  description: "72% cacao dark chocolate",
  price: 4.49,
  effectivePrice: 4.49,
  stockQuantity: 3,
  reservedQuantity: 0,
  availableQuantity: 3,
  imageUrl: "https://picsum.photos/seed/dark-chocolate/400/400",
  status: "ACTIVE",
  category: snacks,
};

const chips = {
  ...baseProduct,
  id: 3,
  name: "Chips",
  description: "Potato chips, 8oz",
  price: 3.99,
  effectivePrice: 3.99,
  stockQuantity: 50,
  reservedQuantity: 0,
  availableQuantity: 50,
  imageUrl: "https://picsum.photos/seed/chips/400/400",
  status: "ACTIVE",
  category: snacks,
};

const allProducts = [sparklingWater, darkChocolate, chips];

function jsonResponse(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function paginated(products: typeof allProducts) {
  return {
    content: products,
    page: 0,
    size: 12,
    totalPages: 1,
    totalElements: products.length,
  };
}

let cartItems: Array<{ id: number; product: typeof sparklingWater; quantity: number }>;
let nextItemId: number;
let addToCartFails: boolean;
let checkoutFails: boolean;
let cartExpired: boolean;
let productsPerPage = 12;

describe("Mini-Mart storefront", () => {
  beforeEach(() => {
    localStorage.clear();
    window.history.pushState({}, "", "/");
    cartItems = [];
    nextItemId = 1;
    addToCartFails = false;
    checkoutFails = false;
    cartExpired = false;
    productsPerPage = 12;

    vi.stubGlobal("fetch", vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
      const url = typeof input === "string" ? input : input.toString();
      if (url.includes("/api/admin/login")) {
        return Promise.resolve(jsonResponse({ token: "test-token" }));
      }
      if (url.includes("/api/admin/me")) {
        return Promise.resolve(jsonResponse({ username: "admin" }));
      }
      if (url.includes("/api/admin/products/") && url.endsWith("/image") && init?.method === "POST") {
        const match = url.match(/\/api\/admin\/products\/(\d+)\/image/);
        const productId = match ? parseInt(match[1]) : 1;
        return Promise.resolve(jsonResponse({ imageUrl: "/uploads/product-" + productId + "-test.jpg" }));
      }
      if (url.includes("/api/admin/products")) {
        return Promise.resolve(jsonResponse(allProducts));
      }
      if (url.includes("/api/admin/categories")) {
        return Promise.resolve(jsonResponse(mockCategories));
      }
      if (url.includes("/api/categories")) {
        return Promise.resolve(jsonResponse(mockCategories));
      }
      if (url === "/api/cart") {
        if (cartExpired) {
          return Promise.resolve(jsonResponse({
            items: cartItems,
            itemCount: cartItems.reduce((s, i) => s + i.quantity, 0),
            expired: true,
          }));
        }
        return Promise.resolve(jsonResponse({
          items: cartItems,
          itemCount: cartItems.reduce((s, i) => s + i.quantity, 0),
          expired: false,
        }));
      }
      if (url.includes("/api/cart/items") && (!init || init.method === "POST")) {
        if (addToCartFails) {
          return Promise.resolve(jsonResponse({ error: "Insufficient stock" }, 400));
        }
        const body = JSON.parse(init?.body as string);
        const product = allProducts.find((p) => p.id === body.productId) || sparklingWater;
        const existing = cartItems.find((i) => i.product.id === body.productId);
        if (existing) {
          existing.quantity += body.quantity || 1;
          return Promise.resolve(jsonResponse(existing, 201));
        }
        const newItem = { id: nextItemId++, product, quantity: body.quantity || 1 };
        cartItems.push(newItem);
        return Promise.resolve(jsonResponse(newItem, 201));
      }
      if (url.includes("/api/cart/items/") && init?.method === "PUT") {
        const match = url.match(/\/api\/cart\/items\/(\d+)/);
        if (match) {
          const itemId = parseInt(match[1]);
          const body = JSON.parse(init.body as string);
          const item = cartItems.find((i) => i.id === itemId);
          if (item) {
            if (body.quantity === 0) {
              cartItems.splice(cartItems.indexOf(item), 1);
              return Promise.resolve(jsonResponse({ status: "removed" }));
            }
            item.quantity = body.quantity;
            return Promise.resolve(jsonResponse(item));
          }
        }
        return Promise.resolve(jsonResponse({ error: "Not found" }, 400));
      }
      if (url.includes("/api/cart/items/") && init?.method === "DELETE") {
        const match = url.match(/\/api\/cart\/items\/(\d+)/);
        if (match) {
          const itemId = parseInt(match[1]);
          const idx = cartItems.findIndex((i) => i.id === itemId);
          if (idx !== -1) cartItems.splice(idx, 1);
        }
        return Promise.resolve(jsonResponse({ status: "removed" }));
      }
      if (url.includes("/api/cart/checkout") && init?.method === "POST") {
        if (checkoutFails) {
          return Promise.resolve(jsonResponse({ error: "Cart is empty" }, 400));
        }
        if (cartItems.length === 0) {
          return Promise.resolve(jsonResponse({ error: "Cart is empty" }, 400));
        }
        const body = init?.body ? JSON.parse(init.body as string) : {};
        const orderItems = cartItems.map((ci) => ({
          id: ci.id,
          product: ci.product,
          quantity: ci.quantity,
          unitPrice: ci.product.price,
        }));
        const total = orderItems.reduce((s, i) => s + i.unitPrice * i.quantity, 0);
        cartItems = [];
        return Promise.resolve(jsonResponse({
          id: 1,
          items: orderItems,
          total,
          status: "PLACED",
          firstName: body.firstName,
          lastName: body.lastName,
          addressLine: body.addressLine,
          city: body.city,
          zipCode: body.zipCode,
          phone1: body.phone1,
          createdAt: new Date().toISOString(),
        }));
      }
      if (url.includes("categoryId=1")) {
        return Promise.resolve(jsonResponse(paginated([sparklingWater])));
      }
      if (url.includes("categoryId=2")) {
        return Promise.resolve(jsonResponse(paginated([darkChocolate])));
      }
      if (url.includes("/api/products") && !url.includes("/api/admin")) {
        const reqUrl = new URL(url, "http://localhost");
        const page = parseInt(reqUrl.searchParams.get("page") || "0");
        const size = productsPerPage;
        const start = page * size;
        const content = allProducts.slice(start, start + size);
        const totalPages = Math.ceil(allProducts.length / size);
        return Promise.resolve(jsonResponse({
          content,
          page,
          size,
          totalPages,
          totalElements: allProducts.length,
        }));
      }
      return Promise.resolve(jsonResponse(allProducts));
    }));
  });

  test("renders Mini-Mart branding and loads products", async () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "Mini-Mart" })).toBeInTheDocument();
    expect(await screen.findByText("Sparkling Water")).toBeInTheDocument();
  });

  test("shows search input and accepts input", async () => {
    render(<App />);
    await screen.findByText("Sparkling Water");

    const searchInput = screen.getByPlaceholderText("Search products...");
    await userEvent.type(searchInput, "chocolate");
    expect(screen.getByDisplayValue("chocolate")).toBeInTheDocument();
  });

  test("category filter buttons work", async () => {
    render(<App />);
    await screen.findByText("Sparkling Water");
    await userEvent.click(screen.getByRole("button", { name: "Beverages" }));
    await waitFor(() => {
      expect(screen.queryByText("Dark Chocolate Bar")).not.toBeInTheDocument();
    });
  });

  test("shows cart button and opens cart panel on add to cart", async () => {
    render(<App />);
    await screen.findByText("Sparkling Water");

    const addButtons = screen.getAllByRole("button", { name: "Add to cart" });
    await userEvent.click(addButtons[0]);

    expect(await screen.findByText("TK 4.99 each")).toBeInTheDocument();
  });

  test("updating cart item quantity", async () => {
    render(<App />);
    await screen.findByText("Sparkling Water");

    const addButtons = screen.getAllByRole("button", { name: "Add to cart" });
    await userEvent.click(addButtons[0]);
    expect(await screen.findByText("TK 4.99 each")).toBeInTheDocument();

    const plusButton = screen.getByText("+");
    await userEvent.click(plusButton);

    expect(await screen.findByText("TK 9.98")).toBeInTheDocument();
  });

  test("shows error on insufficient stock", async () => {
    addToCartFails = true;
    render(<App />);
    await screen.findByText("Sparkling Water");

    const addButtons = screen.getAllByRole("button", { name: "Add to cart" });
    await userEvent.click(addButtons[0]);

    expect(await screen.findByText("Insufficient stock")).toBeInTheDocument();
  });

  test("removing cart item", async () => {
    render(<App />);
    await screen.findByText("Sparkling Water");

    const addButtons = screen.getAllByRole("button", { name: "Add to cart" });
    await userEvent.click(addButtons[0]);
    expect(await screen.findByText("TK 4.99 each")).toBeInTheDocument();

    const removeButton = screen.getByText("Remove");
    await userEvent.click(removeButton);

    expect(await screen.findByText("Your cart is empty.")).toBeInTheDocument();
  });

  test("shows pagination controls when products span multiple pages", async () => {
    productsPerPage = 2;
    render(<App />);
    await screen.findByText("Sparkling Water");

    expect(screen.getAllByText("1").length).toBeGreaterThan(0);
    expect(screen.getAllByText("2").length).toBeGreaterThan(0);
  });

  test("pagination navigates between pages", async () => {
    productsPerPage = 2;
    render(<App />);
    await screen.findByText("Sparkling Water");
    expect(screen.getByText("Dark Chocolate Bar")).toBeInTheDocument();

    await userEvent.click(screen.getByText("2"));
    expect(await screen.findByText("Chips")).toBeInTheDocument();
    expect(screen.queryByText("Sparkling Water")).not.toBeInTheDocument();
  });

  test("admin login page renders at /admin route", async () => {
    window.history.pushState({}, "", "/admin");
    render(<App />);
    expect(screen.getByText("Admin Login")).toBeInTheDocument();
  });

  test("admin login form submits and shows console", async () => {
    window.history.pushState({}, "", "/admin");
    render(<App />);

    await userEvent.type(screen.getByLabelText("Username"), "admin");
    await userEvent.type(screen.getByLabelText("Password"), "admin123");
    await userEvent.click(screen.getByRole("button", { name: "Sign in" }));

    expect(await screen.findByText("Admin Console")).toBeInTheDocument();
  });

  test("admin console shows product table with stock metrics", async () => {
    localStorage.setItem("mini-mart-admin-token", "test-token");
    window.history.pushState({}, "", "/admin");
    render(<App />);

    expect(await screen.findByText("Sparkling Water")).toBeInTheDocument();
    expect(screen.getByText("Dark Chocolate Bar")).toBeInTheDocument();
    expect(screen.getByText("Products")).toBeInTheDocument();
    expect(screen.getByText("Categories")).toBeInTheDocument();
  });

  test("admin console shows low stock warning", async () => {
    localStorage.setItem("mini-mart-admin-token", "test-token");
    window.history.pushState({}, "", "/admin");
    render(<App />);

    expect(await screen.findByText("Low")).toBeInTheDocument();
  });

  test("admin product table shows image thumbnails", async () => {
    localStorage.setItem("mini-mart-admin-token", "test-token");
    window.history.pushState({}, "", "/admin");
    render(<App />);

    await screen.findByText("Sparkling Water");
    const imgs = screen.getAllByRole("img");
    expect(imgs.length).toBeGreaterThan(0);
    expect(imgs[0]).toHaveAttribute("src", sparklingWater.imageUrl);
  });

  test("admin edit form shows image upload for existing product", async () => {
    localStorage.setItem("mini-mart-admin-token", "test-token");
    window.history.pushState({}, "", "/admin");
    render(<App />);

    await screen.findByText("Sparkling Water");
    await userEvent.click(screen.getAllByText("Edit")[0]);

    expect(screen.getByText("Edit Product")).toBeInTheDocument();
    expect(screen.getByText("Choose file")).toBeInTheDocument();
    expect(screen.getByAltText("Sparkling Water")).toBeInTheDocument();
  });

  test("shows checkout button when cart has items", async () => {
    render(<App />);
    await screen.findByText("Sparkling Water");

    const addButtons = screen.getAllByRole("button", { name: "Add to cart" });
    await userEvent.click(addButtons[0]);

    await screen.findByText("TK 4.99 each");
    expect(screen.getByText("Checkout")).toBeInTheDocument();
  });

  test("successful checkout shows order confirmation", async () => {
    render(<App />);
    await screen.findByText("Sparkling Water");

    const addButtons = screen.getAllByRole("button", { name: "Add to cart" });
    await userEvent.click(addButtons[0]);
    await screen.findByText("TK 4.99 each");

    await userEvent.click(screen.getByText("Checkout"));

    expect(await screen.findByText("Review Your Order")).toBeInTheDocument();
    expect(screen.getByText("Confirm Order")).toBeInTheDocument();
    expect(screen.getByText("Go Back")).toBeInTheDocument();

    await userEvent.type(screen.getByLabelText("First Name"), "Jane");
    await userEvent.type(screen.getByLabelText("Last Name"), "Doe");
    await userEvent.type(screen.getByLabelText("Address"), "456 Oak St");
    await userEvent.selectOptions(screen.getByLabelText("City"), "Dhaka");
    await userEvent.type(screen.getByLabelText("Zip Code"), "1205");
    await userEvent.type(screen.getByLabelText("Phone"), "01712345678");

    await userEvent.click(screen.getByText("Confirm Order"));

    expect(await screen.findByText("Order Confirmed")).toBeInTheDocument();
    expect(screen.getByText(/Order #1/)).toBeInTheDocument();
    expect(screen.getByText("Cash on Delivery")).toBeInTheDocument();
    expect(screen.getByText("Jane Doe")).toBeInTheDocument();
    expect(screen.getByText("Dhaka 1205")).toBeInTheDocument();
    expect(screen.getByText("01712345678")).toBeInTheDocument();
    expect(screen.getByText("Continue Shopping")).toBeInTheDocument();
  });

  test("failed checkout shows error message", async () => {
    checkoutFails = true;
    render(<App />);
    await screen.findByText("Sparkling Water");

    const addButtons = screen.getAllByRole("button", { name: "Add to cart" });
    await userEvent.click(addButtons[0]);
    await screen.findByText("TK 4.99 each");

    await userEvent.click(screen.getByText("Checkout"));

    expect(await screen.findByText("Review Your Order")).toBeInTheDocument();

    await userEvent.type(screen.getByLabelText("First Name"), "Jane");
    await userEvent.type(screen.getByLabelText("Last Name"), "Doe");
    await userEvent.type(screen.getByLabelText("Address"), "456 Oak St");
    await userEvent.selectOptions(screen.getByLabelText("City"), "Dhaka");
    await userEvent.type(screen.getByLabelText("Zip Code"), "1205");
    await userEvent.type(screen.getByLabelText("Phone"), "01712345678");

    await userEvent.click(screen.getByText("Confirm Order"));

    expect(await screen.findByText("Cart is empty")).toBeInTheDocument();
  });

  test("expired cart shows expired message", async () => {
    cartExpired = true;
    render(<App />);
    await screen.findByText("Mini-Mart");

    await userEvent.click(screen.getByText("Cart"));

    expect(await screen.findByText("Your cart has expired. Items have been returned to stock.")).toBeInTheDocument();
  });
});
