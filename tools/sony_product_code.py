#!/usr/bin/env python3

import re
import urllib.request
import xml.etree.ElementTree as ET


def http_get(url):
    with urllib.request.urlopen(url) as response:
        return response.read().decode('utf-8')


# 生成列表并排序
product_list = ET.fromstring(
    http_get('https://app.swup.update.sony.net/ess-distribution/public/api/product/v2')
)
model_list = {}
for model in product_list.findall('./models/model'):
    model_list[model.find('name').text] = model.find('Id').text
# 创建mxl
resources = ET.Element('resources')
for model in sorted(model_list.items(), key=lambda x: x[0]):
    model_name = model[0]
    model_id = model[1]
    device_info = ET.fromstring(
        http_get(
            f'https://app.swup.update.sony.net/ess-distribution/public/api/device-service/search?modelObjectId={model_id}'
        )
    )
    if device_info.find('.//sony-product-code') is not None:
        print(f'Model Name: {model_name}, Model ID: {model_id}')
        string_array = ET.SubElement(
            resources, 'string-array', attrib={'name': model_name.replace('-', '_')}
        )
        items = set()
        for customization in device_info.findall('.//customization'):
            customer = customization.find('customer').text.replace(f'{model_name}_', '')
            customer = re.sub('Customized_', '', customer, flags=re.IGNORECASE)
            for product_code in customization.findall('.//sony-product-code'):
                items.add(f'{product_code.text} {customer}')
        for item in sorted(items):
            ET.SubElement(string_array, 'item').text = item

string_array = ET.SubElement(resources, 'string-array', attrib={'name': 'xposed_scope'})
ET.SubElement(string_array, 'item').text = 'com.sonyericsson.updatecenter'
ET.ElementTree(resources).write(
    'sony_product_code.xml', encoding='utf-8', xml_declaration=True
)
