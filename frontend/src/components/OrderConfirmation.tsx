import { useState } from "react";
import { type CartItem, type OrderResponse, type ShippingAddress } from "../api";

type OrderConfirmationProps = {
  items?: CartItem[];
  total?: number;
  order?: OrderResponse;
  shippingAddress?: ShippingAddress;
  onShippingAddressChange?: (address: ShippingAddress) => void;
  onConfirmOrder?: () => void;
  onGoBack?: () => void;
  onContinueShopping?: () => void;
  confirming?: boolean;
  error?: string;
};

type FieldErrors = Partial<Record<keyof ShippingAddress, string>>;

const BANGLADESH_DISTRICTS = [
  "Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur",
  "Manikganj", "Munshiganj", "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail",
  "Bandarban", "Brahmanbaria", "Chandpur", "Chattogram", "Cumilla", "Cox's Bazar",
  "Feni", "Khagrachari", "Lakshmipur", "Noakhali", "Rangamati",
  "Bogura", "Joypurhat", "Naogaon", "Natore", "Chapai Nawabganj", "Pabna", "Rajshahi", "Sirajganj",
  "Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira",
  "Barguna", "Barisal", "Bhola", "Jhalokati", "Patuakhali", "Pirojpur",
  "Habiganj", "Moulvibazar", "Sunamganj", "Sylhet",
  "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchagarh", "Rangpur", "Thakurgaon",
  "Jamalpur", "Mymensingh", "Netrokona", "Sherpur",
];

const requiredFields: (keyof ShippingAddress)[] = ["firstName", "lastName", "addressLine", "city", "zipCode", "phone1"];
const fieldLabels: Record<keyof ShippingAddress, string> = {
  firstName: "First Name", lastName: "Last Name", addressLine: "Address",
  city: "City", zipCode: "Zip Code", phone1: "Phone", phone2: "Phone 2",
};

function validateAddress(address: ShippingAddress): FieldErrors {
  const errors: FieldErrors = {};
  for (const f of requiredFields) {
    if (!(address[f] ?? "").trim()) {
      errors[f] = `${fieldLabels[f]} is required`;
    }
  }
  for (const f of ["phone1", "phone2"] as (keyof ShippingAddress)[]) {
    const val = (address[f] ?? "").trim();
    if (val && !/^\d{11}$/.test(val)) {
      errors[f] = "Must be exactly 11 digits";
    }
  }
  return errors;
}

function AddressForm({
  address,
  onChange,
  errors,
}: {
  address: ShippingAddress;
  onChange: (address: ShippingAddress) => void;
  errors: FieldErrors;
}) {
  function set(field: keyof ShippingAddress, value: string) {
    onChange({ ...address, [field]: value });
  }

  const fields: { key: keyof ShippingAddress; label: string }[] = [
    { key: "firstName", label: "First Name" },
    { key: "lastName", label: "Last Name" },
    { key: "addressLine", label: "Address" },
    { key: "city", label: "City" },
    { key: "zipCode", label: "Zip Code" },
    { key: "phone1", label: "Phone" },
    { key: "phone2", label: "Phone 2" },
  ];

  return (
    <div className="rounded-lg border border-slate-200">
      <div className="border-b border-slate-200 px-6 py-4">
        <h3 className="text-xs font-medium uppercase tracking-wider text-slate-500">Shipping Address</h3>
      </div>
      <div className="px-6 py-4">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {fields.map((f) => {
            const err = errors[f.key];
            return (
              <div key={f.key} className={f.key === "addressLine" ? "sm:col-span-2" : ""}>
                <div className="mb-1 flex items-center gap-0.5">
                  <label htmlFor={f.key} className="text-xs font-medium text-slate-500">{f.label}</label>
                  {requiredFields.includes(f.key) && <span className="text-red-500">*</span>}
                </div>
                {f.key === "city" ? (
                  <select
                    id={f.key}
                    value={address[f.key]}
                    onChange={(e) => set(f.key, e.target.value)}
                    className={`w-full border px-3 py-2 text-sm focus:outline-none ${
                      err ? "border-red-400 focus:border-red-500" : "border-slate-200 focus:border-slate-400"
                    }`}
                  >
                    <option value="">Select a district</option>
                    {BANGLADESH_DISTRICTS.map((d) => (
                      <option key={d} value={d}>{d}</option>
                    ))}
                  </select>
                ) : (
                  <input
                    id={f.key}
                    type={f.key === "phone1" || f.key === "phone2" ? "tel" : "text"}
                    value={address[f.key]}
                    onChange={(e) => set(f.key, e.target.value)}
                    placeholder={f.key === "phone1" || f.key === "phone2" ? "017xxxxxxxx" : undefined}
                    className={`w-full border px-3 py-2 text-sm focus:outline-none ${
                      err ? "border-red-400 focus:border-red-500" : "border-slate-200 focus:border-slate-400"
                    }`}
                  />
                )}
                {err && <p className="mt-1 text-xs text-red-500">{err}</p>}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

function AddressDisplay({ order }: { order: OrderResponse }) {
  return (
    <div className="rounded-lg border border-slate-200">
      <div className="border-b border-slate-200 px-6 py-4">
        <h3 className="text-xs font-medium uppercase tracking-wider text-slate-500">Shipping Address</h3>
      </div>
      <div className="px-6 py-4 text-sm text-slate-700 space-y-1">
        <p className="font-medium text-slate-900">{order.firstName ?? ""} {order.lastName ?? ""}</p>
        <p>{order.addressLine ?? ""}</p>
        <p>{order.city ?? ""} {order.zipCode ?? ""}</p>
        <p>{order.phone1 ?? ""}{order.phone2 ? `  |  ${order.phone2}` : ""}</p>
      </div>
    </div>
  );
}

export default function OrderConfirmation({
  items = [],
  total = 0,
  order,
  shippingAddress,
  onShippingAddressChange,
  onConfirmOrder,
  onGoBack,
  onContinueShopping,
  confirming = false,
  error,
}: OrderConfirmationProps) {
  if (order) {
    return (
      <div className="mx-auto max-w-2xl px-6 py-12">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
            <svg className="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-2xl font-semibold tracking-tight text-slate-900">Order Confirmed</h1>
          <p className="mt-1 text-sm text-slate-500">Thank you for your purchase!</p>
        </div>

        <div className="space-y-4">
          <div className="rounded-lg border border-slate-200">
            <div className="border-b border-slate-200 px-6 py-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-slate-900">Order #{order.id}</span>
                <span className="text-xs uppercase tracking-wider text-slate-500">{order.status}</span>
              </div>
            </div>

            <div className="border-b border-slate-200 px-6 py-4">
              <h3 className="mb-2 text-xs font-medium uppercase tracking-wider text-slate-500">Items</h3>
              <ul className="divide-y divide-slate-100">
                  {order.items.map((item) => (
                    <li key={item.id} className="flex items-center gap-3 py-2">
                      <div className="h-12 w-12 flex-shrink-0 overflow-hidden rounded bg-slate-50">
                        <img src={item.product.imageUrl} alt={item.product.name}
                          className="h-full w-full object-cover" />
                      </div>
                      <div className="flex-1">
                        <p className="text-sm font-medium text-slate-900">{item.product.name}</p>
                        <p className="text-xs text-slate-400">
                          Qty: {item.quantity} &times; TK {item.unitPrice.toFixed(2)}
                          {item.product.onSale && (
                            <span className="ml-1 text-slate-300 line-through">TK {item.product.price.toFixed(2)}</span>
                          )}
                        </p>
                      </div>
                      <p className="text-sm font-medium text-slate-900">TK {(item.quantity * item.unitPrice).toFixed(2)}</p>
                    </li>
                  ))}
              </ul>
            </div>

            <div className="border-b border-slate-200 px-6 py-4">
              <div className="flex items-center justify-between text-sm">
                <span className="text-slate-500">Payment Method</span>
                <span className="font-medium text-slate-900">Cash on Delivery</span>
              </div>
            </div>

            <div className="px-6 py-4">
              <div className="flex items-center justify-between text-base font-semibold text-slate-900">
                <span>Total</span>
                <span>TK {order.total.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {order.firstName && <AddressDisplay order={order} />}
        </div>

        <div className="mt-6 text-center">
          <button onClick={onContinueShopping}
            className="bg-slate-900 px-6 py-2.5 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90">
            Continue Shopping
          </button>
        </div>
      </div>
    );
  }

  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  function handleConfirm() {
    if (!shippingAddress || !onShippingAddressChange) return;
    const errors = validateAddress(shippingAddress);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;
    onConfirmOrder?.();
  }

  function handleAddressChange(addr: ShippingAddress) {
    setFieldErrors({});
    onShippingAddressChange?.(addr);
  }

  return (
    <div className="mx-auto max-w-2xl px-6 py-12">
      <h1 className="mb-8 text-center text-2xl font-semibold tracking-tight text-slate-900">Review Your Order</h1>

      <div className="space-y-6">
        <div className="rounded-lg border border-slate-200">
          <div className="border-b border-slate-200 px-6 py-4">
            <h3 className="text-xs font-medium uppercase tracking-wider text-slate-500">Items ({items.length})</h3>
          </div>

          <div className="px-6 py-4">
            <ul className="divide-y divide-slate-100">
              {items.map((item) => (
                <li key={item.id} className="flex items-center gap-3 py-2">
                  <div className="h-12 w-12 flex-shrink-0 overflow-hidden rounded bg-slate-50">
                    <img src={item.product.imageUrl} alt={item.product.name}
                      className="h-full w-full object-cover" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-slate-900">{item.product.name}</p>
                    <p className="text-xs text-slate-400">Qty: {item.quantity} &times; TK {item.product.effectivePrice.toFixed(2)}</p>
                  </div>
                  <p className="text-sm font-medium text-slate-900">TK {(item.quantity * item.product.effectivePrice).toFixed(2)}</p>
                </li>
              ))}
            </ul>
          </div>

          <div className="border-b border-slate-200 px-6 py-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-500">Payment Method</span>
              <span className="font-medium text-slate-900">Cash on Delivery</span>
            </div>
          </div>

          <div className="px-6 py-4">
            <div className="flex items-center justify-between text-base font-semibold text-slate-900">
              <span>Total</span>
              <span>TK {total.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {shippingAddress && onShippingAddressChange && (
          <AddressForm address={shippingAddress} onChange={handleAddressChange} errors={fieldErrors} />
        )}
      </div>

      {error && (
        <div className="mt-4 rounded border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-600">
          {error}
        </div>
      )}

      <div className="mt-6 flex flex-col items-center gap-3">
        <button onClick={handleConfirm} disabled={confirming}
          className="w-full bg-slate-900 py-2.5 text-xs font-medium uppercase tracking-wider text-white transition-opacity hover:opacity-90 disabled:opacity-50">
          {confirming ? "Placing Order..." : "Confirm Order"}
        </button>
        <button onClick={onGoBack}
          className="text-xs font-medium uppercase tracking-wider text-slate-500 transition-colors hover:text-slate-900">
          Go Back
        </button>
      </div>
    </div>
  );
}
