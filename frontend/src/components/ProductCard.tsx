import { type Product } from "../api";

type ProductCardProps = {
  product: Product;
};

export default function ProductCard({ product }: ProductCardProps) {
  const outOfStock = product.availableQuantity <= 0;

  return (
    <div className="group flex flex-col border border-slate-200 bg-white transition-shadow hover:shadow-sm">
      <div className="aspect-square overflow-hidden bg-slate-50">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="h-full w-full object-cover transition-opacity group-hover:opacity-90"
          loading="lazy"
        />
      </div>

      <div className="flex flex-1 flex-col gap-2 p-4">
        <span className="text-xs font-medium uppercase tracking-wider text-slate-400">
          {product.category.name}
        </span>

        <h3 className="text-base font-medium text-slate-900">{product.name}</h3>

        <p className="text-sm leading-relaxed text-slate-500 line-clamp-2">
          {product.description}
        </p>

        <div className="mt-auto flex items-center justify-between pt-2">
          <span className="text-lg font-semibold text-slate-900">
            ${product.price.toFixed(2)}
          </span>

          <button
            disabled
            className="cursor-not-allowed border border-slate-200 px-3 py-1.5 text-xs font-medium uppercase tracking-wider text-slate-300 transition-colors"
          >
            Add to cart
          </button>
        </div>
      </div>
    </div>
  );
}
