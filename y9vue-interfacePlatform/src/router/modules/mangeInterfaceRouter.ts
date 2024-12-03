const interfaceRouter = {
    path: "/mangeInterface",
    component: () => import("@/layouts/index.vue"),
	name:"mangeInterfaceIndex",
	redirect: "/mangeInterface",
    meta: {
    	title: "管理中心",
    	icon: "ri-user-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/registerInterface",
    		component: () => import("@/views/interface/index.vue"),
    		name: "registerInterface",
			props:{status:"待发布"},
    		meta: { 
				title: "已注册接口",
				icon: "ri-settings-6-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		{
    		path: "/alreadyInterface",
    		component: () => import("@/views/interface/index.vue"),
    		name: "alreadyInterface",
			props:{status:"已发布"},
    		meta: { 
				title: "已发布接口",
				icon: "ri-share-forward-box-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		{
    		path: "/stopInterface",
    		component: () => import("@/views/interface/index.vue"),
    		name: "stopInterface",
			props:{status:"停用"},
    		meta: { 
				title: "已停用接口",
				icon: "ri-indeterminate-circle-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		{
    		path: "/alreadyApply",
    		component: () => import("@/views/interface/index.vue"),
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
				roles: ['none'],
				title: "权限配置管理",
				icon: "ri-share-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	}
    ]
};

export default interfaceRouter;