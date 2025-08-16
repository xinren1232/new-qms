module.exports = {
  root: true,
  env: {
    'browser': true,
    'node': true,
    'es6': true,
    'vue/setup-compiler-macros': true
  },

  parserOptions: {
    parser: '@babel/eslint-parser',
    sourceType: 'module'
  },

  extends: [
    'eslint:recommended',
    'plugin:vue/essential',
    'plugin:vue/recommended',
    'plugin:import/recommended',
    'plugin:import/typescript',
    './.eslint-globals.json'
    // require.resolve('@vue/eslint-config-standard')
  ],

  rules: {
    // 分号
    'semi': ['error', 'never'],
    // 单引号
    'quotes': ['error', 'single', { avoidEscape: true, allowTemplateLiterals: true }],
    // 属性名不能以下划线隔开
    'camelcase': 'off',
    'n/no-callback-literal': 'off',
    // 取消把导入语句提升，否则在设置了 setup 的代码块中会冲突
    'import/first': 'off',
    // 未使用的变量，函数参数里以 _ 开头的变量不会被 eslint 检测
    'no-unused-vars': ['error', { varsIgnorePattern: '^_', argsIgnorePattern: '^_' }],
    // 最后一个逗号
    'comma-dangle': ['error', 'never'],
    // 始终包含 return 语句
    'consistent-return': 'off',
    // 禁止在模板中修改 props
    'vue/no-mutating-props': 'off',
    // 文件最后一行的换行符风格
    'linebreak-style': 'off',
    // 以下划线作为变量开头
    'no-underscore-dangle': 'off',
    // 允许对函数的参数重新赋值
    'no-param-reassign': 'off',
    // 除了 index.vue 文件之外，组件名必须多个单词
    'vue/multi-word-component-names': 'off',
    // ES6 模板字符串的变量空格
    'template-curly-spacing': ['error', 'never'],
    // 对象括号旁边的空格
    'object-curly-spacing': ['error', 'always', { objectsInObjects: true }],
    // 分号前不允许有空格, 但分号后可以有
    'semi-spacing': ['error', { before: false, after: true }],
    // 单行对象可以不换行, 但是一旦换行则必须全部换行
    'object-curly-newline': ['error', { consistent: true }],
    // 不能使用 \ 转义符
    'no-useless-escape': 'off',
    // 文件后缀名
    'import/extensions': 'off',
    // 循环引用
    'import/no-cycle': 'off',
    // 在 template 标签上设置属性
    'vue/no-useless-template-attributes': 'off',
    // 导入文件语句后必须换行
    'import/newline-after-import': ['error'],
    // 不允许重复导入
    'no-duplicate-imports': ['error', { includeExports: true }],
    // 对象键名后的空格
    'key-spacing': ['error', { beforeColon: false, afterColon: true }],
    // if/else 之类的关键字的前后空格
    'keyword-spacing': ['error', { before: true, after: true }],
    // 箭头函数 => 前后的空格
    'arrow-spacing': ['error', { before: true, after: true }],
    // 在局部作用域块前增加空格
    'space-before-blocks': ['error', 'always'],
    // 在函数的括号旁边增加空格
    'space-before-function-paren': 'off',
    // 在括号的旁边增加空格
    'space-in-parens': ['error', 'never'],
    // 在一元/三元操作符前后增加空格
    'space-infix-ops': 'error',
    // 一元操作符前后的空格
    'space-unary-ops': ['error', { words: true, nonwords: false }],
    // 没有多余空格
    'no-multi-spaces': 'error',
    // 在注释中增加空格
    'spaced-comment': ['error', 'always', {
      markers: ['global', 'globals', 'eslint', 'eslint-disable', '*package', '!', ',']
    }],
    // 如果没有找到文件则报错
    'import/no-unresolved': 'off',
    // 不使用默认导出
    'import/no-named-as-default': 'off',
    // 元素标签和内容之间是否必须换行
    'vue/singleline-html-element-content-newline': 'off',
    // 多行标签之间是否必须换行
    'vue/multiline-html-element-content-newline': 'off',
    // 禁用 v-html 指令
    'vue/no-v-html': 'off',
    // 每行最多设置多少个属性
    'vue/max-attributes-per-line': [
      'error',
      {
        singleline: 10,
        multiline: 1
      }
    ],
    'indent': ['error', 2, {
      SwitchCase: 1,
      VariableDeclarator: 1,
      outerIIFEBody: 1,
      MemberExpression: 1,
      FunctionDeclaration: { parameters: 1, body: 1 },
      FunctionExpression: { parameters: 1, body: 1 },
      CallExpression: { arguments: 1 },
      ArrayExpression: 1,
      ObjectExpression: 1,
      ImportDeclaration: 1,
      flatTernaryExpressions: false,
      ignoreComments: false,
      ignoredNodes: [
        'TemplateLiteral *',
        'JSXElement',
        'JSXElement > *',
        'JSXAttribute',
        'JSXIdentifier',
        'JSXNamespacedName',
        'JSXMemberExpression',
        'JSXSpreadAttribute',
        'JSXExpressionContainer',
        'JSXOpeningElement',
        'JSXClosingElement',
        'JSXFragment',
        'JSXOpeningFragment',
        'JSXClosingFragment',
        'JSXText',
        'JSXEmptyExpression',
        'JSXSpreadChild',
        'TSTypeParameterInstantiation',
        'FunctionExpression > .params[decorators.length > 0]',
        'FunctionExpression > .params > :matches(Decorator, :not(:first-child))',
        'ClassBody.body > PropertyDefinition[decorators.length > 0] > .key'
      ],
      offsetTernaryExpressions: true
    }],
    'padding-line-between-statements': [
      'warn',
      {
        blankLine: 'always',
        next: ['block', 'block-like'],
        prev: ['block', 'block-like']
      },
      { blankLine: 'any', next: ['case', 'default'], prev: 'case' },
      { blankLine: 'always', next: 'return', prev: '*' },
      { blankLine: 'always', next: '*', prev: 'directive' },
      { blankLine: 'any', next: 'directive', prev: 'directive' },
      { blankLine: 'always', next: '*', prev: ['const', 'let', 'var'] },
      {
        blankLine: 'any',
        next: ['const', 'let', 'var'],
        prev: ['const', 'let', 'var']
      },
      { blankLine: 'always', next: ['export', 'cjs-export'], prev: '*' },
      {
        blankLine: 'any',
        next: ['export', 'cjs-export'],
        prev: ['export', 'cjs-export']
      },
      { blankLine: 'always', next: '*', prev: ['import', 'cjs-import'] },
      {
        blankLine: 'any',
        next: ['import', 'cjs-import'],
        prev: ['import', 'cjs-import']
      }
    ],
    'sort-imports': 'off',
    'import/order': 'off',
    // 'simple-import-sort/imports': 'error',
    // 'simple-import-sort/exports': 'error',
    // 'sort-destructure-keys/sort-destructure-keys': ['error', { caseSensitive: true }],
    'prefer-promise-reject-errors': 'off'
  }
}
