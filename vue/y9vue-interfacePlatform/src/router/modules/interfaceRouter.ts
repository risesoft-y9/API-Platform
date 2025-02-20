const interfaceRouter = {
    path: "/interface",
    component: () => import("@/layouts/index.vue"),
	name:"interfaceIndex",
	redirect: "/interface",
    meta: {
    	title: "接口集市",
    	icon: "ri-settings-4-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/interface",
    		component: () => import("@/views/interface/index.vue"),
			props:{status:"发布"},
    		name: "interface",
    		meta: { 
				roles: ['V1'],
				title: "接口集市",
				icon: "ri-settings-4-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		{
    		path: "/interface",
    		component: () => import("@/views/interface-v2/index.vue"),
			props:{status:"发布"},
    		name: "interface",
    		meta: { 
				roles: ['V2'],
				title: "接口集市",
				icon: "ri-settings-4-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		
    ]
};

export default interfaceRouter;