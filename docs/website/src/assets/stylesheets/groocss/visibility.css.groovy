// DISPLAY
_.'hide' {
    display 'none !important'
}
// OPACITY
_.'opacity-80-hover' {
    add ':hover', { opacity '80%' }
}
// VISIBILITY-TABLET
media 'only screen and (max-width: 1268px)', {
    _.'hide-tablet' {
        display 'none !important'
    }
}