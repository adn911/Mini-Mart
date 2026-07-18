import { type Product } from "../api";

type ProductCardProps = {
  product: Product;
  onAddToCart: (productId: number) => void;
};

export default function ProductCard({ product, onAddToCart }: ProductCardProps) {
  const outOfStock = product.availableQuantity <= 0;

  return (
    <div className="group flex flex-col border border-slate-200 bg-white transition-shadow hover:shadow-sm">
      <div className="relative aspect-square overflow-hidden bg-slate-50">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="h-full w-full object-cover transition-opacity group-hover:opacity-90"
          loading="lazy"
        />
        {product.onSale && (
          <span className="absolute left-2 top-2 rounded bg-red-500 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wider text-white">
            -{product.discountPercent}%
          </span>
        )}
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
          <div className="flex items-baseline gap-1.5">
            {product.onSale ? (
              <>
                <span className="text-lg font-semibold text-red-600">TK {product.effectivePrice.toFixed(2)}</span>
                <span className="text-sm text-slate-400 line-through">TK {product.price.toFixed(2)}</span>
              </>
            ) : (
              <span className="text-lg font-semibold text-slate-900">TK {product.price.toFixed(2)}</span>
            )}
          </div>

          <button
            onClick={() => onAddToCart(product.id)}
            disabled={outOfStock}
            className={`border px-3 py-1.5 text-xs font-medium uppercase tracking-wider transition-colors ${
              outOfStock
                ? "cursor-not-allowed border-slate-200 text-slate-300"
                : "border-slate-900 text-slate-900 hover:bg-slate-900 hover:text-white"
            }`}
          >
            {outOfStock ? "Out of stock" : "Add to cart"}
          </button>
        </div>
      </div>
    </div>
  );
}
