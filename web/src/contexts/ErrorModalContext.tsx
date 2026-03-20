"use client";

import { createContext, useCallback, useContext, useState } from "react";
import ErrorModal from "@/components/errors/ErrorModal";

interface ErrorModalContextValue {
  showError: (message: string) => void;
}

const ErrorModalContext = createContext<ErrorModalContextValue | null>(null);

export function ErrorModalProvider({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState("");

  const showError = useCallback((msg: string) => {
    setMessage(msg);
    setOpen(true);
  }, []);

  const handleClose = useCallback(() => {
    setOpen(false);
    setMessage("");
  }, []);

  return (
    <ErrorModalContext.Provider value={{ showError }}>
      {children}
      {open && <ErrorModal message={message} onClose={handleClose} />}
    </ErrorModalContext.Provider>
  );
}

export function useErrorModal(): ErrorModalContextValue {
  const ctx = useContext(ErrorModalContext);
  if (!ctx) {
    throw new Error("useErrorModal must be used within ErrorModalProvider");
  }
  return ctx;
}
