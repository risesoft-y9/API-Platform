const interfaceRouter = {
    path: "/blacklisting",
    component: () => import("@/layouts/index.vue"),
	name:"blacklistingIndex",
	redirect: "/blacklisting",
    meta: {
    	title: "黑名单管理",
		roles: ['systemAdmin'],
    	icon: "ri-alert-fill",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/blacklisting",
    		component: () => import("@/views/blacklisting/blacklisting.vue"),
    		name: "blacklisting",
    		meta: { 
				title: "黑名单管理",
				roles: ['systemAdmin'],
				icon: "ri-alert-fill",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		
    ]
};

export default interfaceRouter;