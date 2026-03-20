import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { ErrorModalProvider } from "@/contexts/ErrorModalContext";
import { ProfileProvider } from "@/contexts/ProfileContext";
import Sidebar from "@/components/layout/Sidebar";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "BookBase - Biblioteca Digital",
  description: "Sistema de biblioteca digital - busque, reserve e gerencie empréstimos.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR" className="h-full">
      <body
        className={`${geistSans.variable} ${geistMono.variable} flex h-full min-h-screen antialiased`}
      >
        <ErrorModalProvider>
          <ProfileProvider>
            <Sidebar />
            {/* min-h-0: permite que filhos em flex encolham e o painel lateral (home) não fique com altura 0 no Docker */}
            <div className="flex h-full min-h-0 min-w-0 flex-1 flex-col">
              {children}
            </div>
          </ProfileProvider>
        </ErrorModalProvider>
      </body>
    </html>
  );
}
