const interfaceRouter = {
    path: "/personal",
    component: () => import("@/layouts/index.vue"),
	name:"personalIndex",
	redirect: "/personal",
    meta: {
		roles: ['V2'],
    	title: "个人中心",
    	icon: "ri-user-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/alreadyInterface",
    		component: () => import("@/views/interface-v2/index.vue"),
    		name: "alreadyInterface",
    		meta: { 
				title: "已接入接口",
				icon: "ri-settings-6-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		{
    		path: "/alreadyApply",
    		component: () => import("@/views/interface-v2/index.vue"),
    		name: "alreadyApply",
			props:{status:"申请"},
    		meta: { 
				title: "已申请接口",
				icon: "ri-list-settings-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
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