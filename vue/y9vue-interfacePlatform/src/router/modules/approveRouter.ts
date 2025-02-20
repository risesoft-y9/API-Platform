const approveRouter = {
    path: "/approve",
    component: () => import("@/layouts/index.vue"),
	name:"approveIndex",
	redirect: "/approve",
    meta: {
    	title: "接口审批",
		roles: ['systemAdmin','V2'],
    	icon: "ri-shield-check-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/approve",
    		component: () => import("@/views/approve/index.vue"),
			props:{status:"发布"},
    		name: "approve",
    		meta: { 
				title: "接口审批",
				roles: ['V1'],
				icon: "ri-shield-check-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		{
    		path: "/approve",
    		component: () => import("@/views/approve-v2/index.vue"),
			props:{status:"发布"},
    		name: "approve",
    		meta: { 
				title: "接口审批",
				roles: ['V2'],
				icon: "ri-shield-check-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	}
    ]
};

export default approveRouter;