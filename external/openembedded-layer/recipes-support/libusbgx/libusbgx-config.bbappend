FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI:append:tegra = " \
    file://l4t.schema.in \
    file://usbgx-overrides.conf \
    file://l4t-gadget-config-setup.sh \
"

do_install:append:tegra() {
    install -d ${D}${datadir}/usbgx
    install -m 0644 ${WORKDIR}/l4t.schema.in ${D}${datadir}/usbgx/
    install -d ${D}${sysconfdir}/usbgx
    ln -sf /run/usbgx/l4t.schema ${D}${sysconfdir}/usbgx/
    install -d ${D}${sysconfdir}/systemd/system/usbgx.service.d
    install -m 0644 ${WORKDIR}/usbgx-overrides.conf ${D}${sysconfdir}/systemd/system/usbgx.service.d/
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/l4t-gadget-config-setup.sh ${D}${bindir}/l4t-gadget-config-setup
    sed -i -e's,^IMPORT_SCHEMAS=.*,IMPORT_SCHEMAS="l4t",' ${D}${sysconfdir}/default/usbgx
}

FILES:${PN}:tegra += "${datadir}/usbgx"
PACKAGE_ARCH:tegra = "${MACHINE_ARCH}"
