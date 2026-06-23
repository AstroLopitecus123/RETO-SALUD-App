from PIL import Image
import sys

img_path = r'C:\Users\USER\AndroidStudioProjects\REPOSALUD\REPOSALUD v2.0\app\src\main\res\drawable\logo_solo.png'
try:
    img = Image.open(img_path).convert('RGBA')
    print(f'Size: {img.size}')
    # check if there is transparency
    alpha = img.split()[-1]
    extrema = alpha.getextrema()
    print(f'Alpha extrema: {extrema}')
    
    # Make it white and square
    # First crop to bounding box of non-transparent/non-white?
    # Actually let's just make it white, preserving alpha
    data = img.getdata()
    new_data = []
    for item in data:
        # item is (R,G,B,A)
        # If it's fully transparent, keep it
        # If it's a white background, we might need to turn it transparent
        # Let's check the top-left pixel to see if it's a solid background
        pass
    print(f'Top-left pixel: {data[0]}')
    
    # Let's crop to bounding box of alpha > 0
    bbox = alpha.getbbox()
    print(f'Bbox of alpha: {bbox}')
    if bbox:
        img_cropped = img.crop(bbox)
    else:
        img_cropped = img
        
    # Now if top-left pixel was white and opaque, maybe the whole background is white
    if data[0][3] == 255 and data[0][0] > 240 and data[0][1] > 240 and data[0][2] > 240:
        print('Background seems to be solid white. Attempting to remove it.')
        new_data = []
        for item in img_cropped.getdata():
            if item[0] > 240 and item[1] > 240 and item[2] > 240:
                new_data.append((255, 255, 255, 0))
            else:
                new_data.append((255, 255, 255, item[3]))
        img_cropped.putdata(new_data)
    else:
        # Just turn everything that is opaque to white
        new_data = []
        for item in img_cropped.getdata():
            new_data.append((255, 255, 255, item[3]))
        img_cropped.putdata(new_data)
        
    # Now create a square transparent image that is large enough to add padding
    max_dim = max(img_cropped.size)
    target_size = int(max_dim * 1.5) # 50% padding total
    square = Image.new('RGBA', (target_size, target_size), (0, 0, 0, 0))
    offset = ((target_size - img_cropped.width) // 2, (target_size - img_cropped.height) // 2)
    square.paste(img_cropped, offset)
    
    out_path = r'C:\Users\USER\AndroidStudioProjects\REPOSALUD\REPOSALUD v2.0\app\src\main\res\drawable\logo_launcher_fg.png'
    square.save(out_path)
    print('Successfully generated logo_launcher_fg.png')
except Exception as e:
    print(f'Error: {e}')
