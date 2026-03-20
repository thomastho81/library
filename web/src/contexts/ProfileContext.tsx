"use client";

import { createContext, useCallback, useContext, useState } from "react";

export type ProfileKind = "user" | "gestor";

interface ProfileContextValue {
  profile: ProfileKind;
  userId: number;
  setProfile: (profile: ProfileKind) => void;
}

const ProfileContext = createContext<ProfileContextValue | null>(null);

const USER_ID_USER = 1;
const USER_ID_GESTOR = 2;

export function ProfileProvider({ children }: { children: React.ReactNode }) {
  const [profile, setProfileState] = useState<ProfileKind>("user");

  const setProfile = useCallback((value: ProfileKind) => {
    setProfileState(value);
  }, []);

  const userId = profile === "gestor" ? USER_ID_GESTOR : USER_ID_USER;

  return (
    <ProfileContext.Provider value={{ profile, userId, setProfile }}>
      {children}
    </ProfileContext.Provider>
  );
}

export function useProfile(): ProfileContextValue {
  const ctx = useContext(ProfileContext);
  if (!ctx) {
    throw new Error("useProfile must be used within ProfileProvider");
  }
  return ctx;
}
