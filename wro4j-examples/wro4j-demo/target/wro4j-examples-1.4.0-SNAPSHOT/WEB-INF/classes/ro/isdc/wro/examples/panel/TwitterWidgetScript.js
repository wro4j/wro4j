new TWTR.Widget({
  version: 2,
  type: 'search',
  search: '${search}',
  interval: '${interval}',
  title: '${title}',
  subject: '${subject}',
  width: 272,
  height: 350,
  theme: {
    shell: {
      background: '#8ec1da',
      color: '#ffffff'
    },
    tweets: {
      background: '#ffffff',
      color: '#444444',
      links: '#1985b5'
    }
  },
  features: {
    scrollbar: false,
    loop: true,
    live: true,
    hashtags: true,
    timestamp: true,
    avatars: true,
    behavior: 'default'
  }
}).render().start();