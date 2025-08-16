===============================================
TEST环境：http://10.250.112.66:7700/#/welcome
具体细节Demo 参考官网： http://www.powerjob.tech/
2024年1月24日09点22分 dev配置# 任务调度模块配置
transcend-plm-datadriven-apm项目
注册名称：transcend-plm-datadriven-apm-test
密码：plm@test

alm-transcend-datadriven项目
注册名称：alm-transcend-datadriven
密码：plm@test
 
tr.powerjob.worker.port = 25535
tr.powerjob.worker.app-name = ${spring.application.name}
tr.powerjob.worker.max-appended-wf-context-length = 4096
tr.powerjob.worker.max-result-length = 4096
tr.powerjob.worker.server-address = 10.250.112.66:7700
tr.powerjob.worker.store-strategy = disk
tr.powerjob.worker.enabled = true
===============================================

===============================================
UAT环境：http://10.250.112.66:7700/#/welcome
具体细节Demo 参考官网： http://www.powerjob.tech/
2024年1月24日09点22分 dev配置# 任务调度模块配置
transcend-plm-datadriven-apm项目
注册名称：transcend-plm-datadriven-apm
密码：plm

tr.powerjob.worker.port = 25535
tr.powerjob.worker.app-name = ${spring.application.name}
tr.powerjob.worker.max-appended-wf-context-length = 4096
tr.powerjob.worker.max-result-length = 4096
tr.powerjob.worker.server-address = 10.250.112.66:7700
tr.powerjob.worker.store-strategy = disk
tr.powerjob.worker.enabled = true
===============================================

===============================================
生产环境：http://10.129.1.115:7700/#/welcome
2024年1月24日09点22分 prod 配置# 任务调度模块配置
transcend-plm-datadriven-apm项目
注册名称：transcend-plm-datadriven-apm
密码：plm@prod

tr.powerjob.worker.port = 25535
tr.powerjob.worker.app-name = ${spring.application.name}
tr.powerjob.worker.max-appended-wf-context-length = 4096
tr.powerjob.worker.max-result-length = 4096
tr.powerjob.worker.server-address = 10.129.1.115:7700
tr.powerjob.worker.store-strategy = disk
tr.powerjob.worker.enabled = true
===============================================

===============================================
生产环境_pi：http://10.129.1.115:7700/#/welcome
2024年1月24日09点22分 prod 配置# 任务调度模块配置
transcend-plm-datadriven-pi项目
注册名称：transcend-plm-datadriven-pi
密码：plm@prod

tr.powerjob.worker.port = 25535
tr.powerjob.worker.app-name = ${spring.application.name}
tr.powerjob.worker.max-appended-wf-context-length = 4096
tr.powerjob.worker.max-result-length = 4096
tr.powerjob.worker.server-address = 10.129.1.115:7700
tr.powerjob.worker.store-strategy = disk
tr.powerjob.worker.enabled = true
===============================================
===============================================
生产环境_pi：http://10.129.1.115:7700/#/welcome
2024年1月24日09点22分 prod 配置# 任务调度模块配置
transcend-plm-app-alm项目
注册名称：alm-transcend-datadriven
密码：plm@prod

tr.powerjob.worker.port = 25535
tr.powerjob.worker.app-name = ${spring.application.name}
tr.powerjob.worker.max-appended-wf-context-length = 4096
tr.powerjob.worker.max-result-length = 4096
tr.powerjob.worker.server-address = 10.129.1.115:7700
tr.powerjob.worker.store-strategy = disk
tr.powerjob.worker.enabled = true
===============================================
