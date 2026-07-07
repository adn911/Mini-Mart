Act as an expert Full-Stack Software Engineer and UI/UX Designer. I need you to generate a complete, production-ready blueprint and codebase for an e-commerce website called "Mini-Mart".

### Tech Stack
- Frontend: React with Tailwind CSS (utilizing a modern, ultra-minimalist, sleek design aesthetic)
- Backend: Spring Boot (Java)
- ORM/Database: Hibernate / JPA with an H2 or PostgreSQL database

---

### UI/UX Design System (Crucial Requirement)
The frontend must look highly premium, clean, and modern (inspired by design systems like Linear or Vercel). 
- **Color Palette:** Monochromatic and sophisticated. True black, pure white, and soft shades of slate/gray for subtle borders and backgrounds. Accent colors (like "Success" or interactive states) should be extremely muted or pure black.
- **Typography & Spacing:** Generous whitespace, razor-thin lines (border-slate-100 or dark equivalents), low-radius curves (rounded-md or rounded-lg), and crisp text hierarchy. 
- **Transitions:** Smooth, subtle micro-interactions on hover (`transition-all duration-200 ease-in-out`).

---

### Core Features & Architecture

#### 1. Database & Entities (Spring Boot + Hibernate)
Design the database schema with proper relationships. Provide the Java Entity classes for:
- `Product` (id, name, description, price, stockQuantity, imageUrl)
- `Category` (id, name) — One-to-Many relationship with Product.
- `Order` (id, orderDate, totalAmount, status)
- `OrderItem` (id, quantity, price) — Many-to-One with Product and Order.

#### 2. Backend REST APIs (Spring Boot Controllers)
Provide the REST controllers and service layers handling business logic:
- **Admin Endpoints:** `POST /api/admin/products`, `DELETE /api/admin/products/{id}`, `POST /api/admin/categories`, `DELETE /api/admin/categories/{id}`, `PUT /api/admin/products/{id}/refill`.
- **User Endpoints:** `GET /api/products` (with search/category filtering), `GET /api/categories`, `POST /api/orders` (Must use a database transaction `@Transactional` to safely deduct stock levels and return a 400 Bad Request error if an item goes out of stock).

#### 3. Frontend Frontend UI Components (React + Tailwind)
Provide the frontend code featuring a minimalistic top-bar viewport switcher to jump between "Storefront" and "Admin Console".
- **Storefront Component (Regular User):**
  - **Header:** Sleek typography for "Mini-Mart", a clean inline search bar, and a minimal cart button showing an item count badge.
  - **Product Grid:** Elegant, borderless or cleanly bordered cards. Focus heavily on layout over decoration. Images should be uniformly constrained with a clean aspect ratio. Price tags should be bold and prominent, and "Add to Cart" should be a sleek, solid button that transitions smoothly.
  - **Shopping Cart Panel:** A sliding panel popping out from the right overlaying a blurred backdrop (`backdrop-blur-sm`). Clean item breakdown with tiny +/- increment counters and a monolithic "Confirm Purchase Order" checkout button.
- **Admin Dashboard Component:**
  - A clean, modern data table displaying inventory metrics. Use muted text warnings for "Low Stock" instead of loud, distracting colors.
  - Minimalistic pop-up forms or inline rows to append items, add categories, or tap a quick "Refill (+50)" button.

---

### Deliverables Required
1. **Backend Code:** Provide the `Product` and `Order` Entity classes, the `OrderService` logic handling transactional stock deduction, and the controller mapping.
2. **Frontend Code:** Provide the complete React layout and state management logic. The design must be styled using the Tailwind classes specified in the UI/UX design system section above. Ensure smooth communication between React and Spring Boot via clean fetch/axios structure.
