import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import './App.css'
import { AuthProvider } from './auth/AuthContext'
import Layout from './components/Layout'
import Board from './pages/Board'
import Home from './pages/Home'
import KakaoCallback from './pages/KakaoCallback'
import Login from './pages/Login'
import Matches from './pages/Matches'
import Me from './pages/Me'
import NotFound from './pages/NotFound'
import Players from './pages/Players'
import PostDetail from './pages/PostDetail'
import PostEdit from './pages/PostEdit'

const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/matches', element: <Matches /> },
      { path: '/players', element: <Players /> },
      { path: '/board', element: <Board /> },
      { path: '/board/write', element: <PostEdit /> },
      { path: '/board/:id', element: <PostDetail /> },
      { path: '/board/:id/edit', element: <PostEdit /> },
      { path: '/login', element: <Login /> },
      { path: '/me', element: <Me /> },
      { path: '/auth/kakao/callback', element: <KakaoCallback /> },
      { path: '*', element: <NotFound /> },
    ],
  },
])

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  )
}
