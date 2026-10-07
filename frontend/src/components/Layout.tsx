import { Outlet, ScrollRestoration } from 'react-router-dom'
import BottomNav from './BottomNav'
import Footer from './Footer'
import Header from './Header'

export default function Layout() {
  return (
    <div className="app">
      <Header />
      <main className="main">
        <Outlet />
      </main>
      <Footer />
      <BottomNav />
      <ScrollRestoration />
    </div>
  )
}
