export default {
  // 1.只能是大写字母+下划线
  upperCase: {
    pattern: /^[A-Z_]*$/,
    message: '只能是大写字母+下划线,e.g. USER_NAME'
  },
  // 1.只能是大写字母+下划线+数字+不能以数字开头
  upperCaseAndNumber: {
    pattern: /^[A-Z_][A-Z_0-9]*$/,
    message: '只能是大写字母+下划线+数字+不能以数字开头,e.g. USER_NAME_1'
  },
  // 2.只能是小写字母+驼峰
  lowerCase: {
    pattern: /^[a-z][a-zA-Z]*$/,
    message: '只能是小写字母+驼峰,e.g. userName'
  },
  // 2.只能是小写字母+驼峰+数字+不能以数字开头
  lowerCaseAndNumber: {
    pattern: /^[a-z][a-zA-Z0-9]*$/,
    message: '只能是小写字母+驼峰+数字+不能以数字开头,e.g. userName1'
  },
  // 只能输入汉字,字母,数字,下划线(_)和中划线(-)
  chineseAndEnglishAndNumber: {
    pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9-]*$/,
    message: '只能输入汉字,字母,数字,下划线(_)和中划线(-),e.g. 中国-English-123'
  },
  // 3.只能是数字
  number: {
    pattern: /^[0-9]*$/,
    message: '只能是数字,e.g. 123'
  },
  // 4.只能是数字+小数点
  numberAndPoint: {
    pattern: /^[0-9.]*$/,
    message: '只能是数字+小数点,e.g. 123.45'
  },
  // 5.只能是数字+小数点+负号
  numberAndPointAndNegative: {
    pattern: /^[-0-9.]*$/,
    message: '只能是数字+小数点+负号,e.g. -123.45'
  },
  // 6.只能是数字+小数点+负号+e
  numberAndPointAndNegativeAndE: {
    pattern: /^[-0-9.e]*$/,
    message: '只能是数字+小数点+负号+e,e.g. -123.45e-6'
  },
  // 7.手机号
  phone: {
    pattern: /^1[3456789]\d{9}$/,
    message: '手机号格式不正确'
  },
  // 8.邮箱
  email: {
    pattern: /^([a-zA-Z0-9]+[-_\.]?)+@[a-zA-Z0-9]+\.[a-z]+$/,
    message: '邮箱格式不正确'
  },
  // 9.身份证
  idCard: {
    pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
    message: '身份证格式不正确'
  }
}
