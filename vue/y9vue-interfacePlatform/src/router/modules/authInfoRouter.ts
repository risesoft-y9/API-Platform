const interfaceRouter = {
    path: "/auth",
    component: () => import("@/layouts/index.vue"),
	name:"authIndex",
	redirect: "/auth",
    meta: {
    	title: "权限配置管理",
    	icon: "ri-share-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/authInfo",
    		component: () => import("@/views/auth/index.vue"),
    		name: "authInfo",
    		meta: { 
				title: "权限配置管理",
				icon: "ri-share-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	}
		
    ]
};

export default interfaceRouter;