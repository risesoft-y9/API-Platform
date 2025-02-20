const interfaceRouter = {
    path: "/flow",
    component: () => import("@/layouts/index.vue"),
	name:"flow",
	redirect: "/flow",
    meta: {
		roles: ['V2'],
    	title: "流程配置管理",
    	icon: "ri-settings-3-fill",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/flowInfo",
    		component: () => import("@/views/flownode/flow.vue"),
    		name: "flowInfo",
    		meta: { 
				title: "流程配置管理",
				icon: "ri-settings-3-fill",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	}
		
    ]
};

export default interfaceRouter;