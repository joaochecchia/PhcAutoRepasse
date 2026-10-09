import type { SVGProps } from "react";

export function PickupSideIcon({ size = 24, ...props }: SVGProps<SVGSVGElement> & { size?: number }) {
  return (
    <svg
      {...props}
      aria-hidden="true"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M3 14v-4h9V7h4.4c.8 0 1.5.4 1.9 1.1L20 11h1v3h-2" />
      <path d="M13 14H9M5 14H3M12 10h6.4" />
      <circle cx="7" cy="15" r="2" />
      <circle cx="17" cy="15" r="2" />
    </svg>
  );
}
