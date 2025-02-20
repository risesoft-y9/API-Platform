const interfaceRouter = {
    path: "/executeManger",
    component: () => import("@/layouts/index.vue"),
	name:"executeMangerIndex",
	redirect: "/executeManger",
    meta: {
    	title: "执行端管理",
		roles: ['systemAdmin'],
    	icon: "ri-list-settings-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/executeManger",
    		component: () => import("@/views/execute/index.vue"),
    		name: "executeManger",
    		meta: { 
				title: "执行端管理",
				roles: ['systemAdmin'],
				icon: "ri-list-settings-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		
    ]
};

export default interfaceRouter;