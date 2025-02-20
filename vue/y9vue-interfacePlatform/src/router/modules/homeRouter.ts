const homeRouter = {
    path: "/home",
    component: () => import("@/layouts/index.vue"),
    redirect: "/home",
	name:"homeIndex",
    meta: {
    	title: "首页",
    	icon: "ri-home-8-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/home",
    		component: () => import("@/views/home/home.vue"),
    		name: "home",
    		meta: { 
				title: "首页",
				roles: ['V1'],
				icon: "ri-home-8-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		{
    		path: "/home",
    		component: () => import("@/views/home/home-v2.vue"),
    		name: "home",
    		meta: { 
				title: "首页",
				roles: ['V2'],
				icon: "ri-home-8-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		
    ]
};

export default homeRouter;