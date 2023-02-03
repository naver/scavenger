module.exports = {
    root: true,
    env: {
        browser: true,
        node: true
    },
    globals: {
        "window": true,
        "_": true,
        "_Cookies": true
    },
    extends: [
        'eslint:recommended',
        'plugin:vue/vue3-recommended',
    ],
    rules: {
        // override/add rules settings here, such as:
        // 'vue/no-unused-vars': 'error'
        "linebreak-style": 0,
        "indent": 0,
        "class-methods-use-this": 0,
        "max-len": ["error", { "code": 120 }]
    }
};
