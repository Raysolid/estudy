import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
  ],
  server: {
	  hmr: true,
	  proxy: {
		"/api": {
			  target: import.meta.env.VITE_API_BASE_URL || "http://localhost:8090",
			  changeOrigin: true,
			  pathRewrite: {
				  "^api": "/api"
			  }
		  }
	  }
  }
})
