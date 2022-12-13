def linear(Integer until, Integer index) {
    def value = index > 0 ? (0..until).by(0.4).drop(index - 1).take(1).find() : 0
    return new Measurement(value, 'em')
}

def linear0 = { -> linear(4, 0) }
def linear1 = { -> linear(4, 1) }
def linear2 = { -> linear(4, 2) }
def linear3 = { -> linear(4, 3) }
def linear4 = { -> linear(4, 4) }

_.'mt-0' {
    marginTop linear0()
}

_.'mt-1' {
    marginTop linear1()
}

_.'mt-2' {
    marginTop linear2()
}

_.'mt-3' {
    marginTop linear3()
}

_.'mt-4' {
    marginTop linear4()
}

_.'mb-0' {
    marginBottom linear0()
}

_.'mb-1' {
    marginBottom linear1()
}

_.'mb-2' {
    marginBottom linear2()
}

_.'mb-3' {
    marginBottom linear3()
}

_.'mb-4' {
    marginBottom linear4()
}

_.'pt-0' {
    paddingTop linear0()
}

_.'pt-1' {
    paddingTop linear1()
}

_.'pt-2' {
    paddingTop linear2()
}

_.'pt-3' {
    paddingTop linear3()
}

_.'pt-4' {
    paddingTop linear4()
}

_.'pb-0' {
    paddingBottom linear0()
}

_.'pb-1' {
    paddingBottom linear1()
}

_.'pb-2' {
    paddingBottom linear2()
}

_.'pb-3' {
    paddingBottom linear3()
}

_.'pb-4' {
    paddingBottom linear4()
}

_.'pr-0' {
    paddingRight linear0()
}

_.'pr-1' {
    paddingRight linear2()
}

_.'pr-2' {
    paddingRight linear2()
}

_.'pr-3' {
    paddingRight linear3()
}

_.'pr-4' {
    paddingRight linear4()
}

_.'pl-0' {
    paddingLeft linear0()
}

_.'pl-1' {
    paddingLeft linear1()
}

_.'pl-2' {
    paddingLeft linear2()
}

_.'pl-3' {
    paddingLeft linear3()
}

_.'pl-4' {
    paddingLeft linear4()
}