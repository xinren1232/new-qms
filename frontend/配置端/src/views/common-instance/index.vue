<template>
  <div class="container">
    <el-tooltip content="展开" placement="right">
      <tr-svg-icon v-if="collapseStatus" icon-class="collapse-right" @click="handleCollapse" />
    </el-tooltip>
    <div v-show="!collapseStatus" class="object-tree-box">
      <object-tree @node-click="noTreeNodeClick" />
    </div>
    <div class="instance-box">
      <instance-list
        :height="height"
        :field-list="fieldList"
        :width="width"
        :dict-map="dictMap"
        :life-cycle-data="lifecycleData"
        :super-select-map="superSelectMap"
        :instance-list="instanceListData"
        :object-info="objectInfo"
        :column-rules="validRules"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, provide, ref, watch } from 'vue'
import ObjectTree from './components/object-tree.vue'
import InstanceList from '@/views/view-render/table-x.vue'
import { handleCollapse, collapseStatus } from './components/collapse-controller.js'
import { getViewDetail } from '@/views/system-manage/view-manage/api/index.js'
import { getVersionDetail } from '@/views/system-manage/lifecycle-template/api'
import { queryDictItems } from '@/api/dict'
import { mergeApiNeedUserInfo } from '@/utils'
import mockData from '@@/feature/TrMultiTable/mock/index.js'

const height = computed(() => {
  return document.body.offsetHeight - 70
})
const width = ref(800)

// 当前对象信息，包含绑定的唯一视图、权限、团队、生命周期等信息
const objectInfo = ref({})
const fieldList = ref([])
const lifecycleData = ref([]) // 生命周期数据源
const dictMap = ref({}) // 字典项数据源
const superSelectMap = ref({}) // 超级下拉数据源
const instanceListData = ref(mockData) // 实例列表数据源

const validRules = ref({}) // 校验规则

provide('superSelectMap', superSelectMap)
// TODO
// 1.modeCode 获取视图信息
// 2.获取生命周期模板信息
// 3.获取实例列表信息
// 4.收集字典、超级下拉数据源

watch(
  collapseStatus,
  newVal => {
    if (newVal) {
      width.value = document.body.offsetWidth - 45
    } else {
      width.value = document.body.offsetWidth - 235
    }
  },
  { immediate: true }
)

const noTreeNodeClick = data => {
  objectInfo.value = data
  objectInfo.value.viewBid = '1091061325095899136'
  objectInfo.value.lcTepBid = 'c'
  objectInfo.value.lcTepVersion = 'V17'
  getViewInfo()
  getLifecycleTemplateDetail(objectInfo.value.lcTepBid, objectInfo.value.lcTepVersion)
}

// 获取对象绑定视图配置
const getViewInfo = async () => {
  const { viewBid } = objectInfo.value

  if (!viewBid) return
  const { data } = await getViewDetail(viewBid)

  fieldList.value = data?.content?.propertiesList || []
  console.log(fieldList.value, '---------')
  // fieldList.value[0].type = 'customSpan'
  // fieldList.value[0].field.type = 'customSpan'
  // fieldList.value[0].field.options.type = 'customSpan'

  const rules = {}

  fieldList.value.forEach(item => {
    if (item.required) {
      rules[item.name] = [{ required: true, message: `${item.label}不能为空`, trigger: 'blur' }]
    }
  })
  validRules.value = rules
  console.log(validRules.value, '-----*******-----')

  // 获取绑定的字典下拉数据
  const dictTypeList =
    fieldList.value
      .filter(i => i.type === 'select')
      .map(i => i.field.options.remoteDictType)
      .filter(Boolean) || []
  // 获取人员字段
  const userFieldList = fieldList.value.filter(i => i.type === 'user').map(i => i.field.options.name) || []

  const [{ data: dictList }, { data: tableDataList }] = await Promise.all([
    queryDictItems(dictTypeList),
    mergeApiNeedUserInfo(
      Promise.resolve({
        data: mockData.map(item => {
          item.slider80296 = 0

          return item
        })
      }),
      userFieldList
    )
  ])

  // 字典项数据源
  dictList.forEach(item => {
    dictMap.value[item.code] = item.dictionaryItems
  })
  instanceListData.value = tableDataList
  collectSuperSelectMap()
}
// 收集超级选择器实例数据，批量请求getOne接口,先考虑单对象模式 TODO 多对象的情况后续加进去
const collectSuperSelectMap = () => {
  const superSelectFieldList = fieldList.value.filter(i => i.type === 'super-select').map(i => i.name) || []
  const fieldValueList = []

  superSelectFieldList.forEach(fieldName => {
    const fieldValue = instanceListData.value.map(i => i[fieldName]).filter(Boolean)

    fieldValueList.push(...fieldValue)
  })
  superSelectMap.value = {
    '1108708901215408128': {
      indicator: 'green',
      updated_time: '2023-05-18 10:56:12',
      select002: 'XMJL',
      data_version: '1',
      permissionBid: '1108708901215408128',
      project_name: '2222',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-18 10:53:24',
      id: 8629,
      data_revision: 'A',
      checkoutTime: '2023-05-18 10:54:15',
      app_id: '',
      brand: '["TECNO","Infinix","oraimo"]',
      life_cycle_code: 'underway',
      created_time: '2023-05-18 10:53:24',
      updatedTime: '2023-05-18 10:56:12',
      updatedBy: 'IPMPublicAccount',
      obj_bid: 'technology_project_department',
      isCollect: false,
      plan_complete: 0,
      version: '1',
      created_by: '18651509',
      revision: 'A',
      obj_version: '33',
      condition: '',
      model_code: 'A03001003002',
      platform_code: '94439a39e23248c5b243e83369868e11',
      is_templ: false,
      versions: '1',
      updated_by: 'IPMPublicAccount',
      name: '55624',
      stateCode: 'underway',
      state_code: 'underway',
      type_code: 'RD3',
      model_version: '33',
      permission_bid: '1108708901215408128',
      lc_model_code: 'underway:A03001003002',
      draft_status: '0',
      model_bid: 'technology_project_department',
      android_version: '',
      templ_bid: '1053002003535499264',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: '1111',
      lc_templ_bid: '826107909002891264',
      checkin_description: '',
      oaid: '146006',
      owner: '18651509',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      avatar: 'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632382258228/3%402x.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18651509',
      checkout_time: '2023-05-18 10:54:15',
      bid_1: '1108708901215408128',
      bid: '1108708901215408128',
      storage_domain_id: '1108708904759595008',
      created_by__user__name: '陈月'
    },
    '1108708743895453696': {
      indicator: 'green',
      updated_time: '2023-05-18 10:52:50',
      data_version: '1',
      permissionBid: '1108708743895453696',
      level_code: 'LOW',
      createdTime: '2023-05-18 10:52:50',
      id: 8628,
      data_revision: 'A',
      app_id: '',
      life_cycle_code: 'preparation',
      created_time: '2023-05-18 10:52:50',
      updatedTime: '2023-05-18 10:52:50',
      updatedBy: '18651509',
      obj_bid: '999244585312063488',
      isCollect: false,
      plan_complete: 0,
      version: '1',
      created_by: '18651509',
      revision: 'A',
      obj_version: '8',
      condition: '',
      model_code: 'A03001004',
      is_templ: false,
      versions: '1',
      updated_by: '18651509',
      name: '555623',
      stateCode: 'preparation',
      state_code: 'preparation',
      type_code: 'man3',
      model_version: '8',
      permission_bid: '1108708743895453696',
      lc_model_code: 'preparation:A03001004',
      draft_status: '0',
      model_bid: '999244585312063488',
      android_version: '',
      templ_bid: '1093916782172966912',
      delete: false,
      lc_templ_bid: '826107909002891264',
      checkin_description: '',
      owner: '18651509',
      lc_templ_version: 'V3',
      company_id: 100,
      belonging_domain: '',
      avatar: 'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632382258228/3%402x.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18651509',
      bid_1: '1108708743895453696',
      bid: '1108708743895453696',
      created_by__user__name: '陈月'
    },
    '1108445874838704128': {
      indicator: 'green',
      updated_time: '2023-05-17 18:03:14',
      data_version: '1',
      rd_mode: 'ExternalResearch',
      permissionBid: '1108445874838704128',
      project_name: 'motest2022A',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-17 17:28:36',
      id: 8627,
      data_revision: 'A',
      checkoutTime: '2023-05-17 17:32:21',
      app_id: '',
      brand: 'TECNO',
      life_cycle_code: 'preparation',
      created_time: '2023-05-17 17:28:36',
      updatedTime: '2023-05-17 18:03:14',
      updatedBy: 'IPMPublicAccount',
      obj_bid: 'product_project',
      isCollect: false,
      plan_complete: 0,
      project_code: 'RD02230422',
      version: '1',
      created_by: '18646511',
      revision: 'A',
      obj_version: '49',
      condition: '',
      model_code: 'A03001000',
      platform_code: '6051ac552fc011ec8b8a0242ac110004',
      is_templ: false,
      versions: '1',
      updated_by: 'IPMPublicAccount',
      name: 'motest2022A_sd',
      stateCode: 'preparation',
      state_code: 'preparation',
      type_code: 'IPD_MODULARITY',
      model_version: '49',
      permission_bid: '1108445874838704128',
      lc_model_code: 'preparation:A03001000',
      listed_time: '1685462400000',
      draft_status: '0',
      model_bid: 'product_project',
      android_version: 'Android 13',
      templ_bid: '1022897085445443584',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: 'sd',
      lc_templ_bid: '826107909002891264',
      checkin_description: '',
      motherboard_name: 'sd',
      oaid: '146005',
      owner: '18646511',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      model_type: 'SP',
      avatar:
        'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632384409831/%E7%BB%84%2061%402x%281%29.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18646511',
      checkout_time: '2023-05-17 17:32:21',
      bid_1: '1108445874838704128',
      bid: '1108445874838704128',
      storage_domain_id: '1108445891385233408',
      created_by__user__name: '杨小英'
    },
    '1108355560941686784': {
      indicator: 'green',
      updated_time: '2023-05-17 17:01:11',
      data_version: '1',
      rd_mode: 'SelfResearch',
      permissionBid: '1108355560941686784',
      project_name: '测试项目价值投入',
      product_mission: 'aqddddfwww',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-17 11:29:31',
      id: 8626,
      data_revision: 'A',
      checkoutTime: '2023-05-17 11:32:30',
      app_id: '',
      brand: 'TECNO',
      life_cycle_code: 'preparation',
      created_time: '2023-05-17 11:29:31',
      updatedTime: '2023-05-17 17:01:11',
      updatedBy: '18646511',
      obj_bid: 'product_project',
      isCollect: false,
      plan_complete: 0,
      project_grading: 'B',
      project_code: 'RD02230421',
      version: '1',
      created_by: '18646511',
      market_name: 'aqfd',
      revision: 'A',
      market: 'x',
      obj_version: '49',
      condition: '',
      model_code: 'A03001000',
      platform_code: '60519b0e2fc011ec8b8a0242ac110004',
      is_templ: false,
      versions: '1',
      updated_by: '18646511',
      name: '测试项目价值投入_a',
      stateCode: 'preparation',
      state_code: 'preparation',
      plan_end_time: '2023-06-01 00:00:00',
      type_code: 'IPD_MODULARITY',
      model_version: '49',
      permission_bid: '1108355560941686784',
      lc_model_code: 'preparation:A03001000',
      listed_time: '1685462400000',
      plan_start_time: '2023-05-17 00:00:00',
      description: 'ddddd',
      draft_status: '0',
      model_bid: 'product_project',
      android_version: 'Android 14',
      templ_bid: '1022897085445443584',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: 'as',
      lc_templ_bid: '826107909002891264',
      baseline_name: 'dd',
      checkin_description: '',
      motherboard_name: 'a',
      oaid: '145502',
      owner: '18646511',
      level_coefficient: 'f',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      model_type: 'SP',
      avatar:
        'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632384409831/%E7%BB%84%2061%402x%281%29.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18646511',
      series: 'CAMON',
      checkout_time: '2023-05-17 11:32:30',
      sap_order_number: '000000801590',
      bid_1: '1108355560941686784',
      bid: '1108355560941686784',
      storage_domain_id: '1108355585952321536',
      created_by__user__name: '杨小英'
    },
    '1108029447929466880': {
      indicator: 'green',
      updated_time: '2023-05-16 14:42:33',
      data_version: '1',
      rd_mode: 'SelfResearch',
      permissionBid: '1108029447929466880',
      project_name: '测试项目BBB',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-16 13:53:35',
      id: 8625,
      data_revision: 'A',
      checkoutTime: '2023-05-16 13:54:44',
      app_id: '',
      brand: 'TECNO',
      life_cycle_code: 'preparation',
      created_time: '2023-05-16 13:53:35',
      updatedTime: '2023-05-16 14:42:33',
      updatedBy: '18646511',
      obj_bid: 'product_project',
      isCollect: false,
      plan_complete: 0,
      project_grading: 'B',
      project_code: 'RD02230420',
      version: '1',
      created_by: '18646511',
      market_name: 'E',
      revision: 'A',
      market: 'F',
      obj_version: '49',
      condition: '',
      model_code: 'A03001000',
      platform_code: 'b74060aad3354255b27d96acd8e60227',
      is_templ: false,
      versions: '1',
      updated_by: '18646511',
      name: '测试项目BBB_F',
      stateCode: 'preparation',
      state_code: 'preparation',
      plan_end_time: '2023-05-31 00:00:00',
      type_code: 'IPD_MODULARITY',
      model_version: '49',
      permission_bid: '1108029447929466880',
      lc_model_code: 'preparation:A03001000',
      listed_time: '1685462400000',
      draft_status: '0',
      model_bid: 'product_project',
      android_version: 'Android 14',
      templ_bid: '1022897085445443584',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: 'SD',
      lc_templ_bid: '826107909002891264',
      baseline_name: 'D',
      checkin_description: '',
      motherboard_name: 'F',
      oaid: '145007',
      owner: '18646511',
      level_coefficient: 'F',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      model_type: 'SP',
      avatar:
        'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632384409831/%E7%BB%84%2061%402x%281%29.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18646511',
      series: 'S',
      checkout_time: '2023-05-16 13:54:44',
      sap_order_number: '000000801586',
      bid_1: '1108029447929466880',
      bid: '1108029447929466880',
      storage_domain_id: '1108029455361773568',
      created_by__user__name: '杨小英'
    },
    '1108028144176205824': {
      indicator: 'green',
      updated_time: '2023-05-16 15:05:29',
      data_version: '1',
      rd_mode: 'ExternalResearch',
      permissionBid: '1108028144176205824',
      project_name: '测试项目aaaaa',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-16 13:48:53',
      id: 8624,
      data_revision: 'A',
      checkoutTime: '2023-05-16 13:50:01',
      app_id: '',
      brand: 'Infinix',
      life_cycle_code: 'preparation',
      created_time: '2023-05-16 13:48:53',
      updatedTime: '2023-05-16 15:05:29',
      updatedBy: '18646511',
      obj_bid: 'product_project',
      isCollect: false,
      plan_complete: 0,
      project_grading: 'B',
      project_code: 'RD02230419',
      version: '1',
      created_by: '18646511',
      market_name: 'F',
      revision: 'A',
      market: 'W',
      obj_version: '49',
      condition: '',
      model_code: 'A03001000',
      platform_code: 'b74060aad3354255b27d96acd8e60227',
      is_templ: false,
      versions: '1',
      updated_by: '18646511',
      name: '测试项目aaaaa_A',
      stateCode: 'preparation',
      state_code: 'preparation',
      type_code: 'STRATEGIC',
      model_version: '49',
      permission_bid: '1108028144176205824',
      lc_model_code: 'preparation:A03001000',
      listed_time: '1685462400000',
      draft_status: '0',
      model_bid: 'product_project',
      android_version: 'Android 14',
      templ_bid: '1082252876039983104',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: 'SS',
      lc_templ_bid: '826107909002891264',
      baseline_name: 'SD',
      checkin_description: '',
      motherboard_name: 'A',
      oaid: '145006',
      owner: '18646511',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      model_type: 'SP',
      avatar:
        'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632384465241/%E7%BB%84%2061%402x%20%281%29.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18646511',
      series: 'XS',
      checkout_time: '2023-05-16 13:50:01',
      sap_order_number: '000000801585',
      bid_1: '1108028144176205824',
      bid: '1108028144176205824',
      storage_domain_id: '1108028289877938176',
      created_by__user__name: '杨小英'
    },
    '1107707678127427584': {
      indicator: 'green',
      updated_time: '2023-05-15 16:34:53',
      data_version: '1',
      permissionBid: '1107707678127427584',
      level_code: 'LOW',
      createdTime: '2023-05-15 16:34:53',
      id: 8623,
      data_revision: 'A',
      app_id: '',
      life_cycle_code: 'preparation',
      created_time: '2023-05-15 16:34:53',
      updatedTime: '2023-05-15 16:34:53',
      updatedBy: '18646225',
      obj_bid: 'technology_project',
      isCollect: false,
      plan_complete: 0,
      version: '1',
      created_by: '18646225',
      revision: 'A',
      obj_version: '14',
      condition: '',
      model_code: 'A03001003',
      is_templ: false,
      versions: '1',
      updated_by: '18646225',
      name: '测试测试999',
      stateCode: 'preparation',
      state_code: 'preparation',
      type_code: '',
      model_version: '14',
      permission_bid: '1107707678127427584',
      lc_model_code: 'preparation:A03001003',
      draft_status: '0',
      model_bid: 'technology_project',
      android_version: '',
      templ_bid: '1094951425026428928',
      delete: false,
      lc_templ_bid: '826107909002891264',
      checkin_description: '',
      owner: '18646225',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      avatar: 'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632382258228/3%402x.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18646225',
      bid_1: '1107707678127427584',
      bid: '1107707678127427584',
      created_by__user__name: '张加强'
    },
    '1107701116696662016': {
      indicator: 'green',
      updated_time: '2023-05-16 16:48:10',
      select002: 'createdBy',
      data_version: '1',
      permissionBid: '1107701116696662016',
      project_name: '测试0220',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-15 16:08:49',
      id: 8622,
      data_revision: 'A',
      checkoutTime: '2023-05-15 16:09:26',
      app_id: '',
      brand: '["Infinix","oraimo","TECNO"]',
      life_cycle_code: 'underway',
      created_time: '2023-05-15 16:08:49',
      updatedTime: '2023-05-16 16:48:10',
      updatedBy: '18646225',
      obj_bid: 'technology_project_department',
      isCollect: false,
      plan_complete: 0,
      version: '1',
      created_by: '18650677',
      revision: 'A',
      obj_version: '33',
      condition: '',
      model_code: 'A03001003002',
      platform_code: '94439a39e23248c5b243e83369868e11',
      is_templ: false,
      versions: '1',
      updated_by: '18646225',
      name: '测试0220',
      stateCode: 'underway',
      state_code: 'underway',
      type_code: 'RD3',
      model_version: '33',
      permission_bid: '1107701116696662016',
      lc_model_code: 'underway:A03001003002',
      description: '43',
      draft_status: '0',
      model_bid: 'technology_project_department',
      android_version: '',
      templ_bid: '1053002003535499264',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: "ip'mipm",
      lc_templ_bid: '826107909002891264',
      checkin_description: '',
      owner: '18650677',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      avatar: 'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632382258228/3%402x.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18650677',
      checkout_time: '2023-05-15 16:09:26',
      sap_order_number: '000000801582',
      bid_1: '1107701116696662016',
      bid: '1107701116696662016',
      storage_domain_id: '1107701120014356480',
      created_by__user__name: '白金鹏'
    },
    '1107685124499181568': {
      indicator: 'green',
      updated_time: '2023-05-15 15:08:11',
      data_version: '1',
      rd_mode: 'ExternalResearch',
      permissionBid: '1107685124499181568',
      project_name: '测试qqqa',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-15 15:05:17',
      id: 8621,
      data_revision: 'A',
      checkoutTime: '2023-05-15 15:06:07',
      app_id: '',
      brand: 'itel',
      life_cycle_code: 'preparation',
      created_time: '2023-05-15 15:05:17',
      updatedTime: '2023-05-15 15:08:11',
      updatedBy: 'IPMPublicAccount',
      obj_bid: 'product_project',
      isCollect: false,
      plan_complete: 0,
      project_grading: 'B',
      project_code: 'RD02230418',
      version: '1',
      created_by: '18646511',
      market_name: 's',
      revision: 'A',
      market: 'ee',
      obj_version: '49',
      condition: '',
      model_code: 'A03001000',
      platform_code: '60519b0e2fc011ec8b8a0242ac110004',
      is_templ: false,
      versions: '1',
      updated_by: 'IPMPublicAccount',
      name: '测试qqqa_as',
      stateCode: 'preparation',
      state_code: 'preparation',
      type_code: 'STRATEGIC',
      model_version: '49',
      permission_bid: '1107685124499181568',
      lc_model_code: 'preparation:A03001000',
      listed_time: '1688054400000',
      draft_status: '0',
      model_bid: 'product_project',
      android_version: 'Android 14',
      templ_bid: '1082252876039983104',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: 'k',
      lc_templ_bid: '826107909002891264',
      baseline_name: 'df',
      checkin_description: '',
      motherboard_name: 'as',
      oaid: '145003',
      owner: '18646511',
      level_coefficient: 'ff',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      model_type: 'SP',
      avatar:
        'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632384465241/%E7%BB%84%2061%402x%20%281%29.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18646511',
      series: 'ws',
      checkout_time: '2023-05-15 15:06:07',
      bid_1: '1107685124499181568',
      bid: '1107685124499181568',
      storage_domain_id: '1107685131751133184',
      created_by__user__name: '杨小英'
    },
    '1107678835480072192': {
      indicator: 'green',
      updated_time: '2023-05-15 15:03:25',
      data_version: '1',
      rd_mode: 'SelfResearch',
      permissionBid: '1107678835480072192',
      project_name: 'X6837',
      product_mission: 'ff',
      is_collect: 'false',
      level_code: 'LOW',
      createdTime: '2023-05-15 14:40:22',
      id: 8617,
      data_revision: 'A',
      checkoutTime: '2023-05-15 14:47:50',
      app_id: '',
      brand: 'Infinix',
      life_cycle_code: 'preparation',
      created_time: '2023-05-15 14:40:22',
      updatedTime: '2023-05-15 15:03:25',
      updatedBy: '18646511',
      obj_bid: 'product_project',
      isCollect: false,
      plan_complete: 0,
      project_grading: 'A+',
      project_code: 'RD02230417',
      version: '1',
      created_by: '18646511',
      market_name: 'g',
      revision: 'A',
      obj_version: '49',
      condition: '',
      model_code: 'A03001000',
      platform_code: '60519b0e2fc011ec8b8a0242ac110004',
      is_templ: false,
      versions: '1',
      updated_by: '18646511',
      name: 'X6837_A1',
      stateCode: 'preparation',
      state_code: 'preparation',
      type_code: 'IPD_MODULARITY',
      model_version: '49',
      permission_bid: '1107678835480072192',
      lc_model_code: 'preparation:A03001000',
      listed_time: '1688054400000',
      draft_status: '0',
      model_bid: 'product_project',
      android_version: 'Android 14',
      templ_bid: '1022897085445443584',
      dept_belong:
        '[{"name": "深圳传音控股", "level": 1, "deptNo": "55888888", "masterName": null, "parentDeptNo": "0", "parentDeptName": null, "masterJobNumber": null, "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "传音控股", "level": 2, "deptNo": "55999999", "masterName": null, "parentDeptNo": "55888888", "parentDeptName": null, "masterJobNumber": "0", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "流程与信息中心", "level": 3, "deptNo": "55000013", "masterName": "潘文彦", "parentDeptNo": "55999999", "parentDeptName": null, "masterJobNumber": "18643961", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_业务系统部", "level": 4, "deptNo": "52002525", "masterName": "马秋平", "parentDeptNo": "55000013", "parentDeptName": null, "masterJobNumber": "18600871", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}, {"name": "PI_系统四部", "level": 5, "deptNo": "52002529", "masterName": "刘宝臻", "parentDeptNo": "52002525", "parentDeptName": null, "masterJobNumber": "18644228", "parentDeptMasterName": null, "parentDeptMasterJobNumber": null}]',
      delete: false,
      platform: 'd',
      lc_templ_bid: '826107909002891264',
      baseline_name: 'e',
      checkin_description: '',
      motherboard_name: 'A1',
      oaid: '145002',
      owner: '18646511',
      level_coefficient: '1',
      lc_templ_version: 'V7',
      company_id: 100,
      belonging_domain: '',
      model_type: 'SP',
      avatar:
        'https://transsion-platform01.oss-accelerate.aliyuncs.com/ipm/doc/files/1632384409831/%E7%BB%84%2061%402x%281%29.png',
      is_major_version_upgrade: 'NO',
      is_delete: false,
      createdBy: '18646511',
      series: 'we',
      checkout_time: '2023-05-15 14:47:50',
      sap_order_number: '000000801580',
      bid_1: '1107678835480072192',
      bid: '1107678835480072192',
      storage_domain_id: '1107678842534891520',
      created_by__user__name: '杨小英'
    }
  }

  return [...new Set(fieldValueList.flat(1))]
}
// 获取对象绑定生命周期模板配置
const getLifecycleTemplateDetail = async (bid, version) => {
  const { success, data } = await getVersionDetail(bid, version)

  if (success) {
    const nodeList = data?.layouts
      .map(item => {
        if (item && item.shape === 'rect') return item
      })
      .filter(Boolean)
    const lineList = data?.layouts
      .map(item => {
        if (item.shape === 'edge') return item
      })
      .filter(Boolean)

    // 处理转化线规则
    nodeList.forEach(item => {
      item.nextNodeList = []
      lineList.forEach(line => {
        if (line.source.cell === item.id) {
          item.nextNodeList.push(nodeList.find(node => node.id === line.target.cell)?.data)
        }
      })
    })

    lifecycleData.value = nodeList.map(item => {
      return { ...item.data, nextNodeList: item.nextNodeList, keyCode: item.data.code, zh: item.data.name }
    })
    // 暂时转换成字典的格式

    dictMap.value['__lcTepState__'] = lifecycleData.value
  }
}
</script>
<style scoped lang="scss">
.container {
  display: inline-flex;
  height: 100%;
  .object-tree-box {
    width: 180px;
    overflow: hidden;
    margin-right: var(--margin);
    padding: 10px 0;
    height: 100%;
    max-height: calc(100vh - 20px);
    border-right: 1px solid #dce0e2;
    position: relative;
    .tr-card--container {
      height: 100%;
    }
  }
  .tr-svg-icon {
    margin-left: 5px;
    color: var(--primary);
    font-size: 25px !important;
    cursor: pointer;
    padding: 0 2px;
    position: absolute;
    top: 12px;
    left: -7px;
    &:hover {
      background-color: #eaedec;
      border-radius: 4px;
    }
    &:active {
      background-color: #dee1e0;
    }
  }
  .tree-card {
    padding: var(--padding) 2px;
    box-sizing: border-box;
  }
  .instance-box {
    height: calc(100vh - 20px);
  }
}
</style>
