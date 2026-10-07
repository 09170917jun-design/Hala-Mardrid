import { api } from './client'

export type PostSummary = {
  id: number
  title: string
  authorNickname: string
  createdAt: string
  viewCount: number
  likeCount: number
  commentCount: number
}

export type PostDetail = PostSummary & {
  content: string
  authorId: number
  updatedAt: string
  likedByMe: boolean
}

export type CommentItem = {
  id: number
  authorId: number
  authorNickname: string
  content: string
  createdAt: string
}

export type Page<T> = {
  items: T[]
  page: number
  size: number
  totalPages: number
  totalElements: number
}

export type PostSort = 'latest' | 'popular'

const json = (body: unknown) => ({ body: JSON.stringify(body) })

export const listPosts = (page: number, size = 10, sort: PostSort = 'latest') =>
  api<Page<PostSummary>>(`/api/posts?page=${page}&size=${size}&sort=${sort}`)

export const getPost = (id: number) => api<PostDetail>(`/api/posts/${id}`)

export const createPost = (title: string, content: string) =>
  api<PostDetail>('/api/posts', { method: 'POST', ...json({ title, content }) })

export const updatePost = (id: number, title: string, content: string) =>
  api<PostDetail>(`/api/posts/${id}`, { method: 'PUT', ...json({ title, content }) })

export const deletePost = (id: number) => api<void>(`/api/posts/${id}`, { method: 'DELETE' })

export const toggleLike = (id: number) =>
  api<{ liked: boolean; likeCount: number }>(`/api/posts/${id}/like`, { method: 'POST' })

export const reportPost = (id: number, reason: string) =>
  api<void>(`/api/posts/${id}/report`, { method: 'POST', ...json({ reason }) })

export const listComments = (postId: number) => api<CommentItem[]>(`/api/posts/${postId}/comments`)

export const createComment = (postId: number, content: string) =>
  api<CommentItem>(`/api/posts/${postId}/comments`, { method: 'POST', ...json({ content }) })

export const deleteComment = (id: number) => api<void>(`/api/comments/${id}`, { method: 'DELETE' })
