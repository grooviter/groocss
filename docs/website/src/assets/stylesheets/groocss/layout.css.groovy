_.header {
  backgroundColor 'black'
  minHeight 3.em
  display 'flex'
  justifyContent 'center'
  alignItems 'center'
  paddingRight 2.em
  paddingLeft 2.em

  add '> .menu', {
    width '30%'
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
          add 'i', {
            marginRight 0.2.em
          }
        }
      }
    }
  }
}

_.'content-wrapper' {
  display 'flex'
  justifyContent 'start'
  position 'absolute'
  //backgroundColor '#1b1c1d'

  background "url(pawel-czerwinski-dgJT71cXlC4-unsplash.jpg) no-repeat center center fixed"
  overflow 'hidden'
  top 3.em
  bottom 4.em
  width '100%'
}

_.'contrast-hero-1' {
  position 'absolute'
  top '26%'
  left '10%'
  width 10.em
  minHeight 5.em
  backgroundColor '#aec2ff'
  transform 'rotate(293deg)'
}

_.'contrast-hero-2' {
  position 'absolute'
  top '25%'
  left '27%'
  width 7.5.em
  minHeight 5.em
  backgroundColor '#88d08a'
  transform 'rotate(80deg)'
}

_.hero {
  textAlign 'left'
  position 'relative'
  top '22%'
  left '10%'
  add '> h1', {
    width 11.em
  }

  add '> p', {
    width 20.em
  }

  add '> img', {
    border '1px solid #888'
    borderRadius '100%'
    padding 1.em
    width 200.px
  }
}

_.footer {
  backgroundColor 'black'
  position 'absolute'
  minHeight 4.em
  display 'flex'
  justifyContent 'space-between'
  alignItems 'center'
  bottom 0
  width '100%'
}