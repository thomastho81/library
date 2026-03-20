"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useProfile } from "@/contexts/ProfileContext";
import ProfileSwitcher from "@/components/layout/ProfileSwitcher";

const navItemsUser = [
  { href: "/", label: "Descobrir", icon: "🏠" },
  { href: "/meus-alugueis", label: "Meus alugueis", icon: "📋" },
  { href: "/pesquisa", label: "Pesquisar", icon: "🔍" },
  { href: "/configuracoes", label: "Configurações", icon: "⚙️" },
];

const navItemsGestor = [
  { href: "/inventario", label: "Inventário", icon: "📦" },
  { href: "/devolucoes", label: "Devoluções", icon: "↩️" },
  { href: "/clientes", label: "Clientes", icon: "👥" },
  { href: "/pesquisa", label: "Pesquisar", icon: "🔍" },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { profile } = useProfile();
  const navItems = profile === "gestor" ? navItemsGestor : navItemsUser;

  return (
    <aside className="flex h-full min-h-0 w-56 flex-shrink-0 flex-col self-stretch rounded-r-2xl bg-white py-6 shadow-sm">
      {/* Logo / Nome do sistema */}
      <div className="mb-8 flex items-center gap-2 px-5">
        <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#2563eb] text-lg text-white">
          📖
        </span>
        <span className="text-lg font-semibold text-[#2563eb]">BookBase</span>
      </div>

      <nav className="flex flex-1 flex-col gap-1 px-3">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-[#2563eb] text-white"
                  : "text-gray-600 hover:bg-gray-100 hover:text-gray-900"
              }`}
            >
              <span className="text-base">{item.icon}</span>
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="border-t border-gray-100 px-3 pt-4 space-y-1">
        <div className="px-1">
          <ProfileSwitcher />
        </div>
        <Link
          href="/sair"
          className="flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium text-gray-600 hover:bg-gray-100 hover:text-gray-900"
        >
          <span>🚪</span>
          Sair
        </Link>
      </div>
    </aside>
  );
}
