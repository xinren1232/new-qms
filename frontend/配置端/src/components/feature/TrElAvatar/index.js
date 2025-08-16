import Avatar from './index.vue'

/* istanbul ignore next */
Avatar.install = function (Vue) {
  Vue.component(Avatar.name, Avatar)
}

export default Avatar
