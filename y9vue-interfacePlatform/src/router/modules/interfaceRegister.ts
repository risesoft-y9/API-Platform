const interfaceRegisterRouter = {
    path: "/register",
    component: () => import("@/layouts/index.vue"),
	name:"register",
	redirect: "/register",
    meta: {
    	title: "接口注册",
    	icon: "ri-settings-4-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/interfaceInfo",
    		component: () => import("@/views/interface/interfaceRegister.vue"),
    		name: "interfaceInfo",
    		meta: { 
				title: "接口注册",
				icon: "ri-add-circle-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
		
    ]
};

export default interfaceRegisterRouter;