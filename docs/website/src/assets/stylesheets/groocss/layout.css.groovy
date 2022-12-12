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
  justifyContent 'space-between'
  position 'absolute'
  //backgroundColor '#1b1c1d'
  background "url(pawel-czerwinski-zHXiGy5853Y-unsplash.jpg) no-repeat center center fixed"
  //Photo by <a href="https://unsplash.com/@pawel_czerwinski?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Pawel Czerwinski</a> on <a href="https://unsplash.com/s/photos/geometric-pattern?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText">Unsplash</a>
  overflow 'hidden'
  top 3.em
  bottom 4.em
  width '100%'
}

_.hero {
  textAlign 'center'
  position 'absolute'
  top '50%'
  left '50%'
  transform 'translate(-50%, -60%)'

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