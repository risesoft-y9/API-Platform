const testInterfaceRouter = {
    path: "/testInterface",
    component: () => import("@/layouts/index.vue"),
    redirect: "/testInterface",
	name:"testInterfaceIndex",
    meta: {
    	title: "接口测试",
    	icon: "ri-dashboard-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/testInterface",
    		component: () => import("@/views/interfaceTest/testDialog.vue"),
    		name: "testInterface",
    		meta: { 
				title: "接口测试",
				icon: "ri-dashboard-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		
    ]
};

export default testInterfaceRouter;