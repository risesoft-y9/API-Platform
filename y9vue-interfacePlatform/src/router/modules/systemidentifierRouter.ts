const interfaceRouter = {
    path: "/systemidentifier",
    component: () => import("@/layouts/index.vue"),
	name:"systemidentifierIndex",
	redirect: "/systemidentifier",
    meta: {
    	title: "系统标识管理",
		roles: ['systemAdmin'],
    	icon: "ri-settings-4-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/systemidentifier",
    		component: () => import("@/views/systemidentifier/index.vue"),
    		name: "systemidentifier",
    		meta: { 
				title: "系统标识管理",
				roles: ['systemAdmin'],
				icon: "ri-settings-4-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		
    ]
};

export default interfaceRouter;