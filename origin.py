def ins_magn_pos_KF(a, g, m, use_mag_corrections = True):
	global intermediate_data

	corrections, rpy, a_navs = [], [], []

	C = find_initial_orientation_matrix(np.mean(a[:1000], axis=0))
    #tutaj ladowana jest f-cja interpolujaca pole magnetyczne
	interpolant = pickle.load(open('interpolant.pickle'))
	zv = zupt_test(a)
	print 'C=', C

   
	p_0 = np.array([0.17, -0.17])

	# x = [vx_k, vy_k, px_k, py_k, anavx_k, anavy_k, anavz_k]
	x = np.zeros(shape = (len(a)+1, 2 + 2))
	x[0][2:4] = p_0

	dt = 1./RPS
	p_00 = correct_pos(p_0, m[0], interpolant)


	F = np.vstack([np.hstack([np.eye(2), np.zeros((2,2))]),
	               np.hstack([dt * np.eye(2), np.eye(2)]) ])

	P = np.zeros((4,4))
	kq_v, kq_p = 0.00001, 0.001
	kr = 0.00000001
	Q = dt * (np.diag([ kq_v, kq_v, kq_p, kq_p]))
	R = np.diag([1., 1.]) * kr

	H_pos = np.array([  [0,0, 1,0],
	    				[0,0, 0,1]], dtype = np.float64)

	H_vel = np.array([  [1,0, 0,0],
	    				[0,1, 0,0]], dtype = np.float64)

	for k, (a_k, g_k) in enumerate(zip(a, g)):
		rpy.append(utils.euler_from_matrix(C))
		Omega =  np.array([[ 0.0,  -g_k[2],   g_k[1]],
						  [ g_k[2],   0.0,  -g_k[0]],
						  [-g_k[1],  g_k[0],   0.0]], dtype = np.float64)

		# if not k in zv:
		#from Mechanical Physics
		C = C.dot((2.0*np.eye(3)+(Omega * dt)).dot(
			     np.linalg.inv(2.0*np.eye(3)-(Omega * dt)) ))

		# C = C.dot(np.eye(3) +  Omega*dt[k])
		a_nav = C.dot(a_k)
		a_navs.append(a_nav)

		ax, ay, az = a_nav
		# print F.dot(x[k-1])
		x[k+1] = F.dot(x[k]) +  np.array([dt*ax, dt*ay, 0, 0])# dt*np.eye(2) * a_k[0:2]

		v = np.array([ax, ay]) * dt + x[k, 0:2] #velocity
		p = v * dt + x[k, 2:4] #position

		P = F.dot(P).dot(F.T) + Q
		# intermediate_data.append(P)

        #poprawki magnetyczne co 15-krokow
		if ( (k % 15) == 0) and use_mag_corrections:
			print '--', k,
			# P = (np.eye(9) - K.dot(H)).dot(P)
			# dx = K.dot(xhat[0:3])

			m_c = C.dot(m[k])
			m_c = m[k]
			# m_c = m[k]
			# print m[k], '-->', m_c
			corrections.append( (x[k+1][2:4], m_c) )

			p_tmp = x[k+1][2:4] #p_tmp --> switch lhs to rhs cooridnate system beforem mag correction
			# print p_tmp, '-->',
			# p_tmp[1] = -(p_tmp[1]+0.17) - 0.17
			# print p_tmp
			zk, c_errors = correct_pos(p_tmp, m_c, interpolant) #<----mag correction (interpolant)
			# zk, c_errors = correct_pos_dipole(p_tmp, m_c) #<----mag correction (dipole)
			# if not c_errors:
			# zk[1] = -(zk[1]+0.17) - 0.17

			K = P.dot(H_pos.T).dot( np.linalg.inv(H_pos.dot(P).dot(H_pos.T) + R) )
			P = (np.eye(4) - K.dot(H_pos)).dot(P)
			P = (P + P.T) / 2.0
			yk = zk - H_pos.dot(x[k+1])
			dx = (K.dot(yk))
			xk_ = x[k+1, 2:4] + dx[2:4] #+ [0, 2*0.17]
			vk_ = x[k+1, 0:2] + dx[0:2]
			# print dx[0:2]
			# xk_ = x[k+1, 0:2] #+ 0.01* dx[0:2] #+ [0, 2*0.17]
			# xk_ = x[k+1, 2:4] + (K.dot(yk)[2:4]) #+ [0, 2*0.17]
			# intermediate_data.append((zk, xk_))
			x[k+1, 2:4] = xk_
			x[k+1, 0:2] = vk_

			if make_plot:
				zk, xk_ = map(np.array, zip(*intermediate_data))
				fig = plt.figure()
				plt.plot(zk.T[0], zk.T[1], label = 'zk')
				plt.plot(xk_.T[0], xk_.T[1], label = 'xk_')
				plt.legend()
				fig.savefig('/home/mich/out/' + str(k).rjust(3, '0') + '.png')
				plt.close(fig)
			#plot intermediate
			# print interpolant(zk), m[k, 0:2]
			# print angle_between(m[k, 0:2], interpolant(zk))

			# print  K.dot(x[k+1, 2:4])
			# dx = p - K.dot(x[k+1, 2:4])
			# print dx

			# x[k+1] = x[k+1] - dx
			# x[k+1, 2:4] = p
			# C = utils.R_z(np.deg2rad(yaw)).dot(C)
			# print yaw

			# print x[k+1]
			# print '==========='
		#
		if k in zv:
			# x[k+1, 0:2] = np.zeros(2)

			K = P.dot(H_vel.T).dot( np.linalg.inv(H_vel.dot(P).dot(H_vel.T) + R) ) #kalman gain
			P = (np.eye(4) - K.dot(H_vel)).dot(P)
			P = (P + P.T) / 2.0

			yk = 0 - H_vel.dot(x[k+1])
			# x[k+1, 0:2] = x[k+1,0:2] + K.dot(yk)[0:2]
			x[k+1] = x[k+1] + K.dot(yk) #correct velocity and position
			# x[k+1, 0:2] = np.zeros(2) #correct by simple zero-velocity

			# dx = K.dot(x[k][0:2])
			#
			# vel_error      = dx[0:2]
			# pos_error      = dx[2:4]

			# x[k+1, 0:2] = x[k+1, 0:2] - vel_error
			# x[k+1, 2:4] = x[k+1, 2:4] - pos_error
			# pos_error      = dx[2:4]

		v = x[k+1, 0:2]
		p = x[k+1, 2:4]


		# x[k+1] = np.hstack([v, p, a_nav])

	return x, zv, rpy, corrections, np.array(a_navs)