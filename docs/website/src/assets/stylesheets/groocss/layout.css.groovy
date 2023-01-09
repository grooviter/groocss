_.header {
  minHeight 3.em
  display 'flex'
  justifyContent 'center'
  alignItems 'center'

  media 'only screen and (max-width: 1268px)', {
    _.header {
      add '> .menu', { width '100%' }
    }
  }

  add '> .menu', {
    width '40%'
    display 'flex'
    justifyContent 'center'
    add '> ul', {
      listStyleType 'none'
      display 'flex'
      flexDirection 'row'
      justifyContent 'space-between'
      width '100%'
      add '> li', {
        marginLeft 14.px
        display 'flex'
        alignItems 'center'
        add '> a', {
          textDecoration 'none'
          color '#999'
          add ':hover', { color '#fff' }
          add 'i', { marginRight 0.2.em }
        }
      }
    }
  }
}

_.'content-wrapper' {
  display 'flex'
  justifyContent 'space-between'
  minHeight '80vh'
  width '100%'

  add '.background', {
    background "url(enlsipomy-t4QIHUv8ge0-unsplash.jpg) no-repeat center center fixed"
  }
}

media 'only screen and (max-width: 1268px)', {
  _.'content-wrapper' {
    add '> .hero-wrapper', { width '100%' }
    add '> .contrast-hero', { width '100%' }
  }
}

_.'hero-wrapper' {
  width '50%'
  display 'flex'
  justifyContent 'center'
  alignItems 'center'
  add '> .hero', {
    textAlign 'left'
    add '> h1', { width 11.em }
    add '> p', { width 20.em }
  }
}

_.'logo-wrapper' {
  width '50%'
  display 'flex'
  justifyContent 'center'
  alignItems 'center'
  opacity '60%'
  add '> img', { width 15.em }
}

_.features {
  display 'flex'
  listStyleType 'none'
  flexDirection 'space-around'
  width '100%'
  add '> li', { width '25%' }
}

_.footer {
  position 'relative'
  display 'flex'
  justifyContent 'space-between'
  alignItems 'start'
  width '100%'
  minHeight '16vh'
}

