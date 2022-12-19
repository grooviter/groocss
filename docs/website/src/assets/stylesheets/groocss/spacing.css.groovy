def linear(Integer until, Integer index) {
    def value = index > 0 ? (0..until).by(0.4).drop(index).take(1)?.getAt(0) : 0
    return new Measurement(value, 'em')
}

def linear0 = { -> linear(4, 0) }
def linear1 = { -> linear(4, 1) }
def linear2 = { -> linear(4, 2) }
def linear3 = { -> linear(4, 3) }
def linear4 = { -> linear(4, 4) }
def linear5 = { -> linear(4, 5) }
def linear6 = { -> linear(4, 6) }
def linear7 = { -> linear(4, 7) }
def linear8 = { -> linear(4, 8) }

_.'mt-0' { marginTop linear0() }
_.'mt-1' { marginTop linear1() }
_.'mt-2' { marginTop linear2() }
_.'mt-3' { marginTop linear3() }
_.'mt-4' { marginTop linear4() }
_.'mt-5' { marginTop linear5() }
_.'mt-6' { marginTop linear6() }
_.'mt-7' { marginTop linear7() }
_.'mt-8' { marginTop linear8() }

_.'mb-0' { marginBottom linear0() }
_.'mb-1' { marginBottom linear1() }
_.'mb-2' { marginBottom linear2() }
_.'mb-3' { marginBottom linear3() }
_.'mb-4' { marginBottom linear4() }
_.'mb-5' { marginBottom linear5() }
_.'mb-6' { marginBottom linear6() }
_.'mb-7' { marginBottom linear7() }
_.'mb-8' { marginBottom linear8() }

_.'pt-0' { paddingTop linear0() }
_.'pt-1' { paddingTop linear1() }
_.'pt-2' { paddingTop linear2() }
_.'pt-3' { paddingTop linear3() }
_.'pt-4' { paddingTop linear4() }
_.'pt-5' { paddingTop linear5() }
_.'pt-6' { paddingTop linear6() }
_.'pt-7' { paddingTop linear7() }
_.'pt-8' { paddingTop linear8() }

_.'pb-0' { paddingBottom linear0() }
_.'pb-1' { paddingBottom linear1() }
_.'pb-2' { paddingBottom linear2() }
_.'pb-3' { paddingBottom linear3() }
_.'pb-4' { paddingBottom linear4() }
_.'pb-5' { paddingBottom linear5() }
_.'pb-6' { paddingBottom linear6() }
_.'pb-7' { paddingBottom linear7() }
_.'pb-8' { paddingBottom linear8() }

_.'pr-0' { paddingRight linear0() }
_.'pr-1' { paddingRight linear2() }
_.'pr-2' { paddingRight linear2() }
_.'pr-3' { paddingRight linear3() }
_.'pr-4' { paddingRight linear4() }
_.'pr-5' { paddingRight linear5() }
_.'pr-6' { paddingRight linear6() }
_.'pr-7' { paddingRight linear7() }
_.'pr-8' { paddingRight linear8() }

_.'pl-0' { paddingLeft linear0() }
_.'pl-1' { paddingLeft linear1() }
_.'pl-2' { paddingLeft linear2() }
_.'pl-3' { paddingLeft linear3() }
_.'pl-4' { paddingLeft linear4() }
_.'pl-5' { paddingLeft linear5() }
_.'pl-6' { paddingLeft linear6() }
_.'pl-7' { paddingLeft linear7() }
_.'pl-8' { paddingLeft linear8() }

_.'px-0' { paddingLeft linear0() paddingRight linear0() }
_.'py-0' { paddingBottom linear0() paddingTop linear0() }
_.'px-1' { paddingLeft linear1() paddingRight linear1() }
_.'py-1' { paddingBottom linear1() paddingTop linear1() }
_.'px-2' { paddingLeft linear2() paddingRight linear2() }
_.'py-2' { paddingBottom linear2() paddingTop linear2() }
_.'px-3' { paddingLeft linear3() paddingRight linear3() }
_.'py-3' { paddingBottom linear3() paddingTop linear3() }
_.'px-4' { paddingLeft linear4() paddingRight linear4() }
_.'py-4' { paddingBottom linear4() paddingTop linear4() }