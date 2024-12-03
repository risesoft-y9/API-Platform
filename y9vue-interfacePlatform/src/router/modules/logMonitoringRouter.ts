const logMonitoringRouter = {
    path: "/logMonitoring",
    component: () => import("@/layouts/index.vue"),
    redirect: "/logMonitoring",
	name:"logMonitoringIndex",
    meta: {
    	title: "日志监控",
		roles: ['systemAdmin'],
    	icon: "ri-eye-line",//remix 图标 优先级最高
		// elIcon: "House"//element-plus 图标 优先级第二
    },
    children: [
    	{
    		path: "/logMonitoring",
    		component: () => import("@/views/interface/logMonitoringIndex.vue"),
    		name: "logMonitoring",
    		meta: {
				title: "日志监控",
				roles: ['systemAdmin'],
				icon: "ri-eye-line",//remix 图标 优先级最高
				// elIcon: "House"//element-plus 图标 优先级第二
			},
    	},
    ]
};

export default logMonitoringRouter;