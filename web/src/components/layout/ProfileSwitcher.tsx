"use client";

import { useRef, useState } from "react";
import { useProfile } from "@/contexts/ProfileContext";

export default function ProfileSwitcher() {
  const [profileOpen, setProfileOpen] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);
  const { profile, setProfile } = useProfile();

  return (
    <div className="relative" ref={profileRef}>
      <button
        type="button"
        onClick={() => setProfileOpen((o) => !o)}
        className="flex items-center gap-2 rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 hover:bg-gray-100"
        aria-expanded={profileOpen}
        aria-haspopup="listbox"
        aria-label="Trocar perfil"
      >
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#2563eb] text-sm font-medium text-white">
          {profile === "gestor" ? "G" : "U"}
        </div>
        <span className="text-sm font-medium text-gray-700">
          {profile === "gestor" ? "Gestor" : "Usuário"}
        </span>
        <span className="text-gray-400">▼</span>
      </button>
      {profileOpen && (
        <>
          <div
            className="fixed inset-0 z-10"
            aria-hidden
            onClick={() => setProfileOpen(false)}
          />
          <ul
            role="listbox"
            className="absolute right-0 top-full z-20 mt-1 min-w-[180px] rounded-xl border border-gray-200 bg-white py-1 shadow-lg"
          >
            <li role="option" aria-selected={profile === "user"}>
              <button
                type="button"
                className="w-full px-4 py-2.5 text-left text-sm text-gray-700 hover:bg-gray-50"
                onClick={() => {
                  setProfile("user");
                  setProfileOpen(false);
                }}
              >
                Perfil Usuário
              </button>
            </li>
            <li role="option" aria-selected={profile === "gestor"}>
              <button
                type="button"
                className="w-full px-4 py-2.5 text-left text-sm text-gray-700 hover:bg-gray-50"
                onClick={() => {
                  setProfile("gestor");
                  setProfileOpen(false);
                }}
              >
                Perfil Gestor
              </button>
            </li>
          </ul>
        </>
      )}
    </div>
  );
}
